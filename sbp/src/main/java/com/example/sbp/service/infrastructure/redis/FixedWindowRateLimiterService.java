package com.example.sbp.service.infrastructure.redis;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FixedWindowRateLimiterService implements RateLimiter {
	private final StringRedisTemplate redisTemplate;
	private final int limit = 10;
	private final int windowSeconds = 60;

	public FixedWindowRateLimiterService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public boolean allowRequest(String key) {
		String redisKey = "fw:" + key + ":" + (System.currentTimeMillis() / 1000 / windowSeconds);

		Long count = redisTemplate.opsForValue().increment(redisKey);

		if (count == 1) {
			redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
		}

		return count <= limit;
	}
}
