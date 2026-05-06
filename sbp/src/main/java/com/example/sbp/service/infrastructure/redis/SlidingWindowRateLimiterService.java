package com.example.sbp.service.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;

public abstract class SlidingWindowRateLimiterService implements RateLimiter {
	protected final StringRedisTemplate redisTemplate;
	protected final int limit = 10;
	protected final int windowSeconds = 60;

	public SlidingWindowRateLimiterService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public abstract boolean allowRequest(String key);
}
