package com.example.sbp.dto;

public record TokenDTO (
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds
) {}
