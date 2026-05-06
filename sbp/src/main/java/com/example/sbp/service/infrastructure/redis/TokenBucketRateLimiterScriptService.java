package com.example.sbp.service.infrastructure.redis;

import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class TokenBucketRateLimiterScriptService extends TokenBucketRateLimiterService {
	private final DefaultRedisScript<Long> script;
	public TokenBucketRateLimiterScriptService(StringRedisTemplate redisTemplate) {
		super(redisTemplate);
		this.script = new DefaultRedisScript<>();
		this.script.setResultType(Long.class);
		this.script.setScriptText("""
				-- KEYS[1] = token key
				-- KEYS[2] = timestamp key
				
				-- ARGV[1] = capacity
				-- ARGV[2] = refill_rate (tokens per second)
				-- ARGV[3] = current_time (ms)
				
				local capacity = tonumber(ARGV[1])
				local refill_rate = tonumber(ARGV[2])
				local now = tonumber(ARGV[3])
				
				local tokens = tonumber(redis.call("GET", KEYS[1]))
				if tokens == nil then
				    tokens = capacity
				end
				
				local last_refill = tonumber(redis.call("GET", KEYS[2]))
				if last_refill == nil then
				    last_refill = now
				end
				
				-- refill tokens
				local delta = math.max(0, now - last_refill)
				local refill = delta / 1000.0 * refill_rate
				tokens = math.min(capacity, tokens + refill)
				
				local allowed = 0
				if tokens >= 1 then
				    tokens = tokens - 1
				    allowed = 1
				end
				
				-- persist state
				redis.call("SET", KEYS[1], tokens)
				redis.call("SET", KEYS[2], now)
				
				-- optional TTL to clean idle users
				redis.call("EXPIRE", KEYS[1], 3600)
				redis.call("EXPIRE", KEYS[2], 3600)
				
				return allowed
				""");
	}

	@Override
	public boolean allowRequest(String key) {
		long now = System.currentTimeMillis();

		List<String> keys = List.of(
				"tb:tokens:" + key,
				"tb:ts:" + key
		);

		Long result = redisTemplate.execute(
				script,
				keys,
				String.valueOf(capacity),
				String.valueOf(refillRate),
				String.valueOf(now)
		);

		return result == 1;
	}
}
