package com.grab.service.authorizationserver.adapter.outbound.jdbc;

import com.grab.service.authorizationserver.config.AuthorizationIssuerProperties;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
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
import java.util.HexFormat;
import java.util.List;
import java.util.function.Supplier;

@Component
public class JdbcGrantFamilyStoreAdapter implements GrantFamilyStorePort {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final AuthorizationIssuerProperties properties;

    public JdbcGrantFamilyStoreAdapter(JdbcTemplate jdbc, DataSource dataSource, AuthorizationIssuerProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
        JdbcTransactionManager manager = new JdbcTransactionManager(dataSource);
        this.transaction = new TransactionTemplate(manager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transaction.execute(status -> work.get());
    }

    @Override
    public boolean lock(String familyId) {
        List<String> rows = jdbc.queryForList(
                "SELECT id FROM oauth2_authorization WHERE id = ? FOR UPDATE", String.class, familyId);
        return !rows.isEmpty();
    }

    @Override
    public void consume(String familyId, OAuth2RefreshToken token) {
        String digest = digest(token.getTokenValue());
        Timestamp expires = Timestamp.from(token.getExpiresAt());
        jdbc.update("INSERT INTO oauth2_consumed_refresh (digest, authorization_id, expires_at) VALUES (?, ?, ?)",
                digest, familyId, expires);
    }

    @Override
    public String findConsumedFamily(String token) {
        String digest = digest(token);
        List<String> rows = jdbc.queryForList("""
                SELECT authorization_id FROM oauth2_consumed_refresh
                WHERE digest = ? AND expires_at >= CURRENT_TIMESTAMP
                """, String.class, digest);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Override
    @Scheduled(fixedDelayString = "${grab.authorization-server.cleanup-delay:PT1H}")
    public void cleanExpired() {
        jdbc.update("DELETE FROM oauth2_consumed_refresh WHERE expires_at < CURRENT_TIMESTAMP");
        long retentionSeconds = properties.tokenRetention().toSeconds();
        jdbc.update("""
                DELETE FROM oauth2_authorization
                WHERE COALESCE(refresh_token_expires_at, access_token_expires_at, authorization_code_expires_at)
                    < CURRENT_TIMESTAMP - (? * INTERVAL '1 second')
                """, retentionSeconds);
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
