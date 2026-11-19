package com.grab.service.authorizationserver.core.user;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class JdbcIamUserRepository implements IamUserRepository {
    private final JdbcTemplate jdbc;

    public JdbcIamUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<IamUser> findById(String id) {
        String sql = """
                SELECT id, username, email, password_hash, first_name, last_name, enabled,
                       platform_user_id, authentication_version, created_at, updated_at
                FROM iam_users WHERE id = ?
                """;
        try {
            IamUser user = jdbc.queryForObject(sql, new UserRowMapper(), id);
            return Optional.ofNullable(user).map(this::withAuthorities);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<IamUser> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = """
                SELECT id, username, email, password_hash, first_name, last_name, enabled,
                       platform_user_id, authentication_version, created_at, updated_at
                FROM iam_users WHERE LOWER(email) = LOWER(?)
                """;
        try {
            IamUser user = jdbc.queryForObject(sql, new UserRowMapper(), email.trim());
            return Optional.ofNullable(user).map(this::withAuthorities);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<IamUser> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String sql = """
                SELECT id, username, email, password_hash, first_name, last_name, enabled,
                       platform_user_id, authentication_version, created_at, updated_at
                FROM iam_users WHERE LOWER(username) = LOWER(?)
                """;
        try {
            IamUser user = jdbc.queryForObject(sql, new UserRowMapper(), username.trim());
            return Optional.ofNullable(user).map(this::withAuthorities);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<IamUser> findByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.isBlank()) return Optional.empty();
        String trimmed = identifier.trim();
        String sql = """
                SELECT id, username, email, password_hash, first_name, last_name, enabled,
                       platform_user_id, authentication_version, created_at, updated_at
                FROM iam_users WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)
                """;
        try {
            IamUser user = jdbc.queryForObject(sql, new UserRowMapper(), trimmed, trimmed);
            return Optional.ofNullable(user).map(this::withAuthorities);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public IamUser save(IamUser user) {
        String sql = """
                INSERT INTO iam_users (id, username, email, password_hash, first_name, last_name,
                                      enabled, platform_user_id, authentication_version, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    username = EXCLUDED.username,
                    email = EXCLUDED.email,
                    password_hash = EXCLUDED.password_hash,
                    first_name = EXCLUDED.first_name,
                    last_name = EXCLUDED.last_name,
                    enabled = EXCLUDED.enabled,
                    platform_user_id = EXCLUDED.platform_user_id,
                    authentication_version = EXCLUDED.authentication_version,
                    updated_at = EXCLUDED.updated_at
                """;

        Instant now = Instant.now();
        Timestamp created = user.createdAt() != null ? Timestamp.from(user.createdAt()) : Timestamp.from(now);
        Timestamp updated = Timestamp.from(now);

        jdbc.update(sql,
                user.id(),
                user.username(),
                user.email(),
                user.passwordHash(),
                user.firstName(),
                user.lastName(),
                user.enabled(),
                user.platformUserId(),
                user.authenticationVersion(),
                created,
                updated
        );

        // Save authorities
        if (user.authorities() != null && !user.authorities().isEmpty()) {
            jdbc.update("DELETE FROM iam_user_authorities WHERE user_id = ?", user.id());
            for (String auth : user.authorities()) {
                jdbc.update("INSERT INTO iam_user_authorities (user_id, authority) VALUES (?, ?) ON CONFLICT DO NOTHING",
                        user.id(), auth);
            }
        }

        return findById(user.id()).orElse(user);
    }

    @Override
    public void updatePlatformUserId(String id, String platformUserId) {
        jdbc.update("UPDATE iam_users SET platform_user_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                platformUserId, id);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        Integer count = jdbc.queryForObject("SELECT count(*) FROM iam_users WHERE LOWER(email) = LOWER(?)",
                Integer.class, email.trim());
        return count != null && count > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) return false;
        Integer count = jdbc.queryForObject("SELECT count(*) FROM iam_users WHERE LOWER(username) = LOWER(?)",
                Integer.class, username.trim());
        return count != null && count > 0;
    }

    private IamUser withAuthorities(IamUser user) {
        List<String> authList = jdbc.query("SELECT authority FROM iam_user_authorities WHERE user_id = ?",
                (rs, rowNum) -> rs.getString("authority"), user.id());
        Set<String> authorities = new HashSet<>(authList);
        return new IamUser(
                user.id(),
                user.username(),
                user.email(),
                user.passwordHash(),
                user.firstName(),
                user.lastName(),
                user.enabled(),
                user.platformUserId(),
                user.authenticationVersion(),
                authorities,
                user.createdAt(),
                user.updatedAt()
        );
    }

    private static class UserRowMapper implements RowMapper<IamUser> {
        @Override
        public IamUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp created = rs.getTimestamp("created_at");
            Timestamp updated = rs.getTimestamp("updated_at");
            return new IamUser(
                    rs.getString("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getBoolean("enabled"),
                    rs.getString("platform_user_id"),
                    rs.getLong("authentication_version"),
                    Set.of(),
                    created != null ? created.toInstant() : null,
                    updated != null ? updated.toInstant() : null
            );
        }
    }
}
