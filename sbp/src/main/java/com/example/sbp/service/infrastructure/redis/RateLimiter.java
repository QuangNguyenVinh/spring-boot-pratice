package com.example.sbp.service.infrastructure.redis;

public interface RateLimiter {
	boolean allowRequest(String key);
}
