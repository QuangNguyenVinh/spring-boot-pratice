package com.example.sbp.service.infrastructure.redis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class LeakyBucketRateLimiterService implements RateLimiter {

	private final StringRedisTemplate redisTemplate;

	private final int capacity = 10;
	private final int leakRatePerSec = 5;

	public LeakyBucketRateLimiterService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public boolean allowRequest(String key) {
		String redisKey = "lb:" + key;

		long now = System.currentTimeMillis();

		ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();

		long leakBefore = now - 1000;
		zSet.removeRangeByScore(redisKey, 0, leakBefore);

		Long size = zSet.zCard(redisKey);

		if (size >= capacity) {
			return false;
		}

		zSet.add(redisKey, UUID.randomUUID().toString(), now);
		redisTemplate.expire(redisKey, leakRatePerSec, TimeUnit.SECONDS);

		return true;
	}
}
