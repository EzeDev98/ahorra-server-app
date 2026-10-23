package com.ahorra.common.util;

import com.ahorra.common.exception.MaxAttemptExceededException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final RedissonClient redissonClient;

    public void checkRateLimit(String key, int maxRequests, Duration window) {
        RRateLimiter limiter = redissonClient.getRateLimiter(key);
        limiter.trySetRate(RateType.OVERALL, maxRequests, window.getSeconds(), RateIntervalUnit.SECONDS);
        limiter.expire(window);

        if (!limiter.tryAcquire()) {
            throw new MaxAttemptExceededException("Too many requests. Please try again later.");
        }
    }
}
