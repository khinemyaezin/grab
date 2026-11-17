package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

final class SellerPendingRevocations {
    private static final String KEY = "grab:seller:{revocations}";
    private static final Duration TTL = Duration.ofDays(7);

    private static final DefaultRedisScript<Long> ENQUEUE = new DefaultRedisScript<>("""
            redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[2])
            redis.call('ZADD', KEYS[1], ARGV[3], ARGV[4])
            return 1
            """, Long.class);
    private static final DefaultRedisScript<Long> CLAIM = new DefaultRedisScript<>("""
            local untilTime = redis.call('ZSCORE', KEYS[1], ARGV[1])
            if not untilTime or tonumber(untilTime) > tonumber(ARGV[2]) then return 0 end
            redis.call('ZADD', KEYS[1], ARGV[3], ARGV[1])
            return 1
            """, Long.class);
    private static final DefaultRedisScript<Long> REMOVE = new DefaultRedisScript<>("""
            redis.call('ZREM', KEYS[1], ARGV[1])
            return redis.call('DEL', KEYS[2])
            """, Long.class);

    private final StringRedisTemplate redis;
    private final SellerIssuerClient issuer;
    private final MeterRegistry metrics;

    SellerPendingRevocations(StringRedisTemplate redis, SellerIssuerClient issuer, MeterRegistry metrics) {
        this.redis = redis;
        this.issuer = issuer;
        this.metrics = metrics;
    }

    void enqueue(String sessionId, String refreshToken) {
        Instant now = Instant.now();
        long next = now.plusSeconds(30).getEpochSecond();
        List<String> keys = List.of(KEY, slot(sessionId));
        redis.execute(ENQUEUE, keys, refreshToken, Long.toString(TTL.toSeconds()), Long.toString(next), sessionId);
        metrics.counter("seller.oidc.revocation.queued").increment();
    }

    @Scheduled(fixedDelayString = "${grab.seller-oidc.revocation-retry-delay:PT30S}")
    void retry() {
        long now = Instant.now().getEpochSecond();
        ZSetOperations<String, String> pending = redis.opsForZSet();
        Set<String> candidates = pending.rangeByScore(KEY, 0, now, 0, 25);
        if (candidates == null) {
            return;
        }
        for (String sessionId : candidates) {
            Long claimed = redis.execute(CLAIM, List.of(KEY), sessionId, Long.toString(now), Long.toString(now + 60));
            if (Long.valueOf(1).equals(claimed)) {
                revoke(sessionId);
            }
        }
    }

    private void revoke(String sessionId) {
        String key = slot(sessionId);
        String token = redis.opsForValue().get(key);
        try {
            if (token != null) {
                issuer.revoke(token);
            }
            redis.execute(REMOVE, List.of(KEY, key), sessionId);
        } catch (SellerOidcFailure unavailable) {
            metrics.counter("seller.oidc.revocation.retry").increment();
        }
    }

    private String slot(String sessionId) {
        return KEY + ":" + sessionId;
    }
}
