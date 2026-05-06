package com.example.sbp.service.infrastructure.redis;

import java.util.List;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

public class TokenBucketRateLimiterCodingService extends TokenBucketRateLimiterService {

	public TokenBucketRateLimiterCodingService(StringRedisTemplate redisTemplate) {
		super(redisTemplate);
	}

	@Override
	public boolean allowRequest(String key) {
		String tokenKey = "tb:tokens:" + key;
		String timestampKey = "tb:ts:" + key;

		long now = System.currentTimeMillis();

		List<Object> result = redisTemplate.execute((RedisCallback<? extends List<Object>>) connection -> {
			byte[] tk = tokenKey.getBytes();
			byte[] ts = timestampKey.getBytes();

			byte[] tokenBytes = connection.stringCommands().get(tk);
			byte[] tsBytes = connection.stringCommands().get(ts);

			double tokens = tokenBytes == null ? capacity : Double.parseDouble(new String(tokenBytes));
			long lastRefill = tsBytes == null ? now : Long.parseLong(new String(tsBytes));

			double newTokens = tokens + (now - lastRefill) / 1000.0 * refillRate;
			tokens = Math.min(capacity, newTokens);

			boolean allowed = tokens >= 1;
			if (allowed) tokens -= 1;

			connection.stringCommands().set(tk, String.valueOf(tokens).getBytes());
			connection.stringCommands().set(ts, String.valueOf(now).getBytes());

			return List.of(allowed ? 1 : 0);
		});

		return ((Long) result.get(0)) == 1;
	}
}
