package com.example.sbp.service;

import com.example.sbp.dto.TokenDTO;
import com.example.sbp.exception.UnauthorizedException;
import com.example.sbp.model.AuthSession;
import com.example.sbp.model.RefreshToken;
import com.example.sbp.repository.RefreshTokenRepository;
import com.example.sbp.security.component.JwtService;
import com.example.sbp.security.component.RefreshTokenGenerator;
import com.example.sbp.security.model.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TokenService {

    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(@Value("${jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds,
                        @Value("${jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds,
                        AppUserService appUserService, JwtService jwtService,
                        RefreshTokenGenerator refreshTokenGenerator, RefreshTokenRepository refreshTokenRepository) {
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.appUserService = appUserService;
        this.jwtService = jwtService;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public TokenDTO issueOnLogin(UserPrincipal principal, AuthSession authSession) throws NoSuchAlgorithmException {
        // 1) Create DB row FIRST (no JWT yet)
        RefreshToken rt = new RefreshToken();
        rt.setUser(authSession.getUser());
        rt.setSession(authSession);
        rt.setIssuedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        rt.setRevoked(false);

        // temporary placeholder hash (will be replaced)
        rt.setTokenHash(new byte[32]);

        rt = refreshTokenRepository.save(rt); // DB generates uuidv7()

        // 2) Now sign refresh JWT with DB-generated ID as jti
        String refreshJwt = jwtService.issueRefreshToken(
                principal.getUserId(),
                principal.getUsername(),
                authSession.getId(),
                rt.getId()
        );

        // 3) Hash real JWT and update row
        rt.setTokenHash(refreshTokenGenerator.hash(refreshJwt));
        refreshTokenRepository.save(rt);

        // 4) Issue access token
        String access = jwtService.issueAccessToken(principal);

        return new TokenDTO(access, refreshJwt, accessTokenTtlSeconds);
    }

    @Transactional
    public TokenDTO rotateRefresh(String refreshJwt) throws Exception {
        var jwt = jwtService.parseAndValidate(refreshJwt);

        if (!"refresh".equals(jwt.getClaimAsString("type"))) {
            throw new UnauthorizedException("Not a refresh token");
        }

        byte[] hash = refreshTokenGenerator.hash(refreshJwt);

        RefreshToken old = refreshTokenRepository.findByHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        // Replay detection
        if (old.getRevoked()) {
            refreshTokenRepository.revokeSession(old.getSession().getId(), "REPLAY_DETECTED");
            throw new UnauthorizedException("Refresh token replay");
        }

        // Hard consistency: JWT jti must match DB row
        UUID jti = UUID.fromString(jwt.getId());
        if (!old.getId().equals(jti)) {
            refreshTokenRepository.revokeSession(old.getSession().getId(), "JTI_MISMATCH");
            throw new UnauthorizedException("Token mismatch");
        }

        // 1) Create new refresh token row FIRST
        RefreshToken next = new RefreshToken();
        next.setUser(old.getUser());
        next.setSession(old.getSession());
        next.setIssuedAt(Instant.now());
        next.setExpiresAt(Instant.now().plusSeconds(refreshTokenTtlSeconds));
        next.setRevoked(false);
        next.setTokenHash(new byte[32]); // placeholder

        next = refreshTokenRepository.save(next);

        // 2) Sign new refresh JWT
        String newRefresh = jwtService.issueRefreshToken(
                old.getUser().getId(),
                jwt.getSubject(),
                old.getSession().getId(),
                next.getId()
        );

        // 3) Update new hash
        next.setTokenHash(refreshTokenGenerator.hash(newRefresh));
        refreshTokenRepository.save(next);

        // 4) Rotate old -> new
        int updated = refreshTokenRepository.rotate(
                old.getId(),
                next.getId(),
                "ROTATED"
        );

        if (updated != 1) {
            refreshTokenRepository.revokeSession(old.getSession().getId(), "CONCURRENT_ROTATION");
            throw new UnauthorizedException("Refresh token already used");
        }

        // 5) New access token
        UserPrincipal principal = appUserService.loadUserById(old.getUser().getId());
        String access = jwtService.issueAccessToken(principal);

        return new TokenDTO(access, newRefresh, accessTokenTtlSeconds);
    }
}
