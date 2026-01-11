package com.example.sbp.security.component;

import com.example.sbp.security.model.UserPrincipal;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;


@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String issuer;
    @Getter
    private final long accessTokenTtlSeconds;
    @Getter
    private final long refreshTokenTtlSeconds;

    public JwtService(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.rsa.public-key}") RSAPublicKey rsaPublicKey,
            @Value("${jwt.rsa.private-key}") RSAPrivateKey rsaPrivateKey,
            @Value("${jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds,
            @Value("${jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds
    ) {
        this.issuer = issuer;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        var jwk = new RSAKey.Builder(rsaPublicKey)
                .privateKey(rsaPrivateKey)
                .build();
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        this.jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }

    public String issueAccessToken(UserPrincipal principal) {
        var now = Instant.now();
        var exp = now.plusSeconds(accessTokenTtlSeconds);

        var authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(principal.getUsername())
                .issuedAt(now)
                .expiresAt(exp)
                .id(UUID.randomUUID().toString())          // jti
                .claim("uid", principal.getUserId().toString())
                .claim("auth", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    public String issueRefreshToken(UUID userId, String username, UUID sessionId, UUID tokenId) {
        var now = Instant.now();
        var exp = now.plusSeconds(refreshTokenTtlSeconds);
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(now)
                .expiresAt(exp)
                .id(tokenId.toString())                    // jti
                .claim("uid", userId.toString())
                .claim("sid", sessionId.toString())
                .claim("type", "refresh")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    public Jwt parseAndValidate(String token) {
        var jwt = jwtDecoder.decode(token);

        if (!issuer.equals(jwt.getIssuer().toString())) {
            throw new JwtException("Invalid issuer");
        }

        return jwt;
    }
}
