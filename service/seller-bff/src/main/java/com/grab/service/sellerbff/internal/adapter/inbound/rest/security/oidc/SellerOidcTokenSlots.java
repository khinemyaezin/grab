package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerIssuerClient.Tokens;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

final class SellerOidcTokenSlots {
    private static final Duration DEFAULT_TOKEN_TTL = Duration.ofDays(7);

    private static final DefaultRedisScript<Long> RENEW = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end
            return redis.call('PEXPIRE', KEYS[1], ARGV[2])
            """, Long.class);

    private static final DefaultRedisScript<Long> WRITE = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end
            if ARGV[2] == '' then
                redis.call('DEL', KEYS[2])
            else
                redis.call('SET', KEYS[2], ARGV[2], 'PX', ARGV[3])
            end
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> UNLOCK = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end
            return redis.call('DEL', KEYS[1])
            """, Long.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final String prefix;
    private final Duration ttl;

    SellerOidcTokenSlots(StringRedisTemplate redis, ObjectMapper json, SellerOidcProperties properties) {
        this.redis = redis;
        this.json = json;
        prefix = properties.redisNamespace() + ":tokens:";
        Duration timeout = properties.sessionTimeout();
        ttl = timeout.compareTo(DEFAULT_TOKEN_TTL) > 0 ? timeout : DEFAULT_TOKEN_TTL;
    }

    void markLoggedOut(String sessionId) {
        String marker = sessionKey(sessionId) + ":logout";
        redis.opsForValue().set(marker, "1", ttl);
    }

    void requireActive(String sessionId) {
        String marker = sessionKey(sessionId) + ":logout";
        if (Boolean.TRUE.equals(redis.hasKey(marker))) {
            throw SellerOidcFailure.invalid();
        }
    }

    Tokens read(String sessionId) {
        String encoded = redis.opsForValue().get(slotKey(sessionId));
        if (encoded == null) {
            return null;
        }
        try {
            return json.readValue(encoded, Tokens.class);
        } catch (JsonProcessingException exception) {
            throw SellerOidcFailure.unavailable();
        }
    }

    void start(String sessionId, Tokens tokens) {
        String jsonTokens = encode(tokens);
        redis.opsForValue().set(slotKey(sessionId), jsonTokens, ttl);
    }

    <T> T locked(String sessionId, Function<Slot, T> function) {
        String key = sessionKey(sessionId) + ":lock";
        String owner = UUID.randomUUID().toString();
        Boolean acquired = redis.opsForValue().setIfAbsent(key, owner, SellerOidcProperties.LOCK_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            throw SellerOidcFailure.busy();
        }
        try {
            return function.apply(new Slot(sessionId, key, owner));
        } finally {
            redis.execute(UNLOCK, List.of(key), owner);
        }
    }

    final class Slot {
        private final String sessionId;
        private final String key;
        private final String owner;

        private Slot(String sessionId, String key, String owner) {
            this.sessionId = sessionId;
            this.key = key;
            this.owner = owner;
        }

        Tokens read() {
            return SellerOidcTokenSlots.this.read(sessionId);
        }

        void renew() {
            String leaseMillis = Long.toString(SellerOidcProperties.LOCK_TTL.toMillis());
            List<String> keys = List.of(key);
            Long renewed = redis.execute(RENEW, keys, owner, leaseMillis);
            requireOwnership(renewed);
        }

        void write(Tokens tokens) {
            String value = tokens == null ? "" : encode(tokens);
            String ttlMillis = Long.toString(ttl.toMillis());
            String tokenKey = slotKey(sessionId);
            List<String> keys = List.of(key, tokenKey);
            Long updated = redis.execute(WRITE, keys, owner, value, ttlMillis);
            requireOwnership(updated);
        }

        private void requireOwnership(Long result) {
            if (!Long.valueOf(1).equals(result)) {
                throw SellerOidcFailure.unavailable();
            }
        }
    }

    private String slotKey(String sessionId) {
        return sessionKey(sessionId) + ":value";
    }

    private String sessionKey(String sessionId) {
        return prefix + "{session:" + sessionId + "}";
    }

    private String encode(Tokens tokens) {
        try {
            return json.writeValueAsString(tokens);
        } catch (JsonProcessingException exception) {
            throw SellerOidcFailure.unavailable();
        }
    }
}
