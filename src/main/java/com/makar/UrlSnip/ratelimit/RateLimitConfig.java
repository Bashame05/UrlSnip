package com.makar.UrlSnip.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {
    private final Map<String , Bucket> bucketMap = new ConcurrentHashMap<>();
    public Bucket getBucket(String ip) {
        return bucketMap.computeIfAbsent(ip, key ->
                Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(10)
                                .refillIntervally(10, Duration.ofMinutes(1))
                                .build())
                        .build());
    }
}
