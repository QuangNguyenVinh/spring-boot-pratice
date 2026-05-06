package com.example.sbp.service.infrastructure.redis;

import java.util.Collections;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class SlidingWindowRateLimiterScriptService extends SlidingWindowRateLimiterService {

	private final DefaultRedisScript<Long> script;

	public SlidingWindowRateLimiterScriptService(StringRedisTemplate redisTemplate) {
		super(redisTemplate);
		this.script = new DefaultRedisScript<>();
		this.script.setResultType(Long.class);
		this.script.setScriptText("""
					-- KEYS[1] = sorted set key
				
					-- ARGV[1] = current_time (ms)
					-- ARGV[2] = window_size (ms)
					-- ARGV[3] = limit
				
					local now = tonumber(ARGV[1])
					local window = tonumber(ARGV[2])
					local limit = tonumber(ARGV[3])
				
					local min = now - window
				
					-- remove old entries
					redis.call("ZREMRANGEBYSCORE", KEYS[1], 0, min)
				
					-- current count
					local count = redis.call("ZCARD", KEYS[1])
				
					if count >= limit then
						return 0
					end
				
					-- add new request
					redis.call("ZADD", KEYS[1], now, now)
				
					-- set TTL
					redis.call("EXPIRE", KEYS[1], math.ceil(window / 1000))
				
					return 1
				""");
	}

	@Override
	public boolean allowRequest(String key) {
		String redisKey = "sw:lua:" + key;

		String requestId = UUID.randomUUID().toString();

		Long result = redisTemplate.execute(
				script,
				Collections.singletonList(redisKey),
				String.valueOf(limit),
				String.valueOf(windowSeconds),
				requestId
		);

		return result == 1;
	}
}
