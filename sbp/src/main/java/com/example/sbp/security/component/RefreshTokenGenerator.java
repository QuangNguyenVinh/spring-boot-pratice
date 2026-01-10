package com.example.sbp.security.component;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RefreshTokenGenerator {

    private static final SecureRandom RNG = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[64];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public byte[] hash(String token) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
    }
}

