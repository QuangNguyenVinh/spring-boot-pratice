package com.example.sbp.service.infrastructure.redis;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class SlidingWindowRateLimiterCodingService extends SlidingWindowRateLimiterService{

	public SlidingWindowRateLimiterCodingService(StringRedisTemplate redisTemplate) {
		super(redisTemplate);
	}

	@Override
	public boolean allowRequest(String key) {
		String redisKey = "sw:" + key;
		long now = System.currentTimeMillis();
		long windowStart = now - windowSeconds * 1000;

		ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();
		zSet.removeRangeByScore(redisKey, 0, windowStart);
		zSet.add(redisKey, String.valueOf(now), now);

		Long count = zSet.zCard(redisKey);
		redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);

		return count <= limit;
	}
}
