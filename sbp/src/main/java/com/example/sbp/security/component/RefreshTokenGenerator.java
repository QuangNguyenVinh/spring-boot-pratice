package com.example.sbp.security.component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenGenerator {

    private static final SecureRandom RNG = new SecureRandom();

    public byte[] hash(String token) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
    }
}

