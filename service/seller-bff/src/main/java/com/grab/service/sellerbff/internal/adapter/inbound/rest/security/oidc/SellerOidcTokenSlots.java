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
    private static final String PREFIX = "grab:seller:tokens:";
    private static final Duration TTL = Duration.ofDays(7);
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);

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

    SellerOidcTokenSlots(StringRedisTemplate redis, ObjectMapper json) {
        this.redis = redis;
        this.json = json;
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
        redis.opsForValue().set(slotKey(sessionId), jsonTokens, TTL);
    }

    <T> T locked(String sessionId, Function<Slot, T> function) {
        String key = PREFIX + "{session:" + sessionId + "}:lock";
        String owner = UUID.randomUUID().toString();
        Boolean acquired = redis.opsForValue().setIfAbsent(key, owner, LOCK_TTL);
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

        void write(Tokens tokens) {
            String value = tokens == null ? "" : encode(tokens);
            String ttl = Long.toString(TTL.toMillis());
            Long updated = redis.execute(WRITE, List.of(key, slotKey(sessionId)), owner, value, ttl);
            if (!Long.valueOf(1).equals(updated)) {
                throw SellerOidcFailure.unavailable();
            }
        }
    }

    private String slotKey(String sessionId) {
        return PREFIX + "{session:" + sessionId + "}:value";
    }

    private String encode(Tokens tokens) {
        try {
            return json.writeValueAsString(tokens);
        } catch (JsonProcessingException exception) {
            throw SellerOidcFailure.unavailable();
        }
    }
}
