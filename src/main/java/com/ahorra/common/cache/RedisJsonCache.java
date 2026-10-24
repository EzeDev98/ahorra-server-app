package com.ahorra.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisJsonCache {

    private final RedissonClient redisson;
    private final ObjectMapper mapper;

    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            RBucket<String> bucket = redisson.getBucket(key, StringCodec.INSTANCE);
            String json = bucket.get();
            return json == null ? Optional.empty() : Optional.of(mapper.readValue(json, type));
        } catch (Exception e) {
            log.warn("Cache read failed for {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    public void put(String key, Object value, Duration ttl) {
        try {
            RBucket<String> bucket = redisson.getBucket(key, StringCodec.INSTANCE);
            bucket.set(mapper.writeValueAsString(value), ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Cache write failed for {}: {}", key, e.getMessage());
        }
    }

    public void evict(String key) {
        try {
            RBucket<String> bucket = redisson.getBucket(key, StringCodec.INSTANCE);
            bucket.delete();
        } catch (Exception e) {
            log.warn("Cache evict failed for {}: {}", key, e.getMessage());
        }
    }
}
