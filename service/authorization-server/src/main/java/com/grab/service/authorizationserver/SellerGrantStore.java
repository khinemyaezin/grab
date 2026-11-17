package com.grab.service.authorizationserver;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Supplier;

@Component
final class SellerGrantStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    SellerGrantStore(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        JdbcTransactionManager manager = new JdbcTransactionManager(dataSource);
        transaction = new TransactionTemplate(manager);
    }

    <T> T transaction(Supplier<T> work) {
        return transaction.execute(status -> work.get());
    }

    boolean lock(String familyId) {
        List<String> rows = jdbc.queryForList(
                "SELECT id FROM oauth2_authorization WHERE id = ? FOR UPDATE", String.class, familyId);
        return !rows.isEmpty();
    }

    void consume(String familyId, OAuth2RefreshToken token) {
        String digest = digest(token.getTokenValue());
        Timestamp expires = Timestamp.from(token.getExpiresAt());
        jdbc.update("INSERT INTO seller_consumed_refresh (digest, authorization_id, expires_at) VALUES (?, ?, ?)",
                digest, familyId, expires);
    }

    String consumedFamily(String token) {
        String digest = digest(token);
        List<String> rows = jdbc.queryForList("""
                SELECT authorization_id FROM seller_consumed_refresh
                WHERE digest = ? AND expires_at >= CURRENT_TIMESTAMP
                """, String.class, digest);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Scheduled(fixedDelayString = "${grab.seller-issuer.cleanup-delay:PT1H}")
    void cleanExpired() {
        jdbc.update("DELETE FROM seller_consumed_refresh WHERE expires_at < CURRENT_TIMESTAMP");
        jdbc.update("""
                DELETE FROM oauth2_authorization
                WHERE COALESCE(refresh_token_expires_at, access_token_expires_at, authorization_code_expires_at)
                    < CURRENT_TIMESTAMP - INTERVAL '1 day'
                """);
    }

    private static String digest(String token) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
