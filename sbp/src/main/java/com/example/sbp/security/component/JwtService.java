package com.example.sbp.security.component;

import com.example.sbp.security.model.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    @Getter
    private final long accessTokenTtlSeconds;
    @Getter
    private final long refreshTokenTtlSeconds;

    public JwtService(
            @Value("${jwt.secret-base64}") String secretBase64,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds,
            @Value("${jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
        this.issuer = issuer;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    public String issueAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenTtlSeconds);

        List<String> auths = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .issuer(issuer)
                .subject(principal.getUsername())
                .id(UUID.randomUUID().toString()) // jti
                .claim("uid", principal.getUserId().toString())
                .claim("auth", auths)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String issueRefreshToken(UUID userId, String username, UUID sessionId, UUID tokenId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTokenTtlSeconds);
        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("uid", userId.toString())
                .claim("sid", sessionId.toString())
                .claim("type", "refresh")
                .id(tokenId.toString())          // jti
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }
}
