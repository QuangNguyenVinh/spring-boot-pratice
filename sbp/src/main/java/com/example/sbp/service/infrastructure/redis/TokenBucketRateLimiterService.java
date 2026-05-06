package com.example.sbp.service.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;

public abstract class TokenBucketRateLimiterService implements RateLimiter {
	protected final StringRedisTemplate redisTemplate;

	protected final int capacity = 10;
	protected final double refillRate = 1.0; // tokens per second

	public TokenBucketRateLimiterService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}


	public abstract boolean allowRequest(String key);
}
