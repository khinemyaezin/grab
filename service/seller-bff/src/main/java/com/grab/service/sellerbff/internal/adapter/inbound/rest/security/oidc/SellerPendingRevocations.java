package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerIssuerClient.Tokens;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

final class SellerPendingRevocations {
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
    private final SellerOidcTokenSlots slots;
    private final String queueKey;
    private final String logoutQueueKey;
    private final String tokenTtlSeconds;
    private final long claimLeaseSeconds;
    private final int batchSize;

    SellerPendingRevocations(StringRedisTemplate redis, SellerIssuerClient issuer, MeterRegistry metrics,
                             SellerOidcTokenSlots slots, SellerOidcProperties properties) {
        this.redis = redis;
        this.issuer = issuer;
        this.metrics = metrics;
        this.slots = slots;
        queueKey = properties.redisNamespace() + ":{revocations}";
        logoutQueueKey = queueKey + ":logout";
        long tokenTtl = properties.revocationTokenTtl().toSeconds();
        tokenTtlSeconds = Long.toString(tokenTtl);
        claimLeaseSeconds = properties.revocationClaimLease().toSeconds();
        batchSize = properties.revocationBatchSize();
    }

    void enqueueToken(String refreshToken) {
        String item = "token:" + UUID.randomUUID();
        Instant now = Instant.now();
        List<String> keys = List.of(queueKey, slot(item));
        String due = Long.toString(now.getEpochSecond());
        redis.execute(ENQUEUE, keys, refreshToken, tokenTtlSeconds, due, item);
        metrics.counter("seller.oidc.revocation.queued").increment();
    }

    void enqueueLogout(String sessionId) {
        Instant now = Instant.now();
        ZSetOperations<String, String> pending = redis.opsForZSet();
        pending.addIfAbsent(logoutQueueKey, sessionId, now.getEpochSecond());
        slots.markLoggedOut(sessionId);
        metrics.counter("seller.oidc.revocation.queued").increment();
    }

    @Scheduled(fixedDelayString = "${grab.seller-oidc.revocation-retry-delay}")
    void retry() {
        retry(logoutQueueKey, this::revokeSession);
        retry(queueKey, this::revokeToken);
    }

    private void retry(String queue, Consumer<String> revoke) {
        long now = Instant.now().getEpochSecond();
        ZSetOperations<String, String> pending = redis.opsForZSet();
        Set<String> candidates = pending.rangeByScore(queue, 0, now, 0, batchSize);
        if (candidates == null) {
            return;
        }
        for (String item : candidates) {
            long claimTime = Instant.now().getEpochSecond();
            String due = Long.toString(claimTime);
            String lease = Long.toString(claimTime + claimLeaseSeconds);
            Long claimed = redis.execute(CLAIM, List.of(queue), item, due, lease);
            if (Long.valueOf(1).equals(claimed)) {
                try {
                    revoke.accept(item);
                } catch (SellerOidcFailure failure) {
                    metrics.counter("seller.oidc.revocation.retry").increment();
                }
            }
        }
    }

    private void revokeToken(String item) {
        String key = slot(item);
        String token = redis.opsForValue().get(key);
        if (token != null) {
            issuer.revoke(token);
        }
        redis.execute(REMOVE, List.of(queueKey, key), item);
    }

    private void revokeSession(String sessionId) {
        slots.markLoggedOut(sessionId);
        slots.locked(sessionId, slot -> {
            Tokens tokens = slot.read();
            if (tokens != null) {
                slot.renew();
                issuer.revoke(tokens.refresh());
                slot.write(null);
            }
            return null;
        });
        ZSetOperations<String, String> pending = redis.opsForZSet();
        pending.remove(logoutQueueKey, sessionId);
    }

    private String slot(String item) {
        return queueKey + ":" + item;
    }
}
