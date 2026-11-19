package com.grab.service.authorizationserver.core.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Repository
public class JdbcIamAccessContextRepository implements IamAccessContextRepository {
    private final JdbcTemplate jdbc;

    public JdbcIamAccessContextRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<IamAccessContext> findByUserId(String userId) {
        String sql = """
                SELECT id, user_id, assignment_id, scope_key, scope_id, role, display_name, authorities, synced_at
                FROM iam_user_access_contexts WHERE user_id = ?
                ORDER BY scope_key, scope_id
                """;
        return jdbc.query(sql, new ContextRowMapper(), userId);
    }

    @Override
    public void replaceUserContexts(String userId, List<IamAccessContext> contexts) {
        jdbc.update("DELETE FROM iam_user_access_contexts WHERE user_id = ?", userId);
        if (contexts == null || contexts.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO iam_user_access_contexts (id, user_id, assignment_id, scope_key, scope_id,
                                                     role, display_name, authorities, synced_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Instant now = Instant.now();
        Timestamp synced = Timestamp.from(now);

        for (IamAccessContext ctx : contexts) {
            String authoritiesStr = ctx.authorities() != null ? String.join(",", ctx.authorities()) : "";
            String id = ctx.id() != null ? ctx.id() : UUID.randomUUID().toString();
            jdbc.update(sql,
                    id,
                    userId,
                    ctx.assignmentId(),
                    ctx.scopeKey(),
                    ctx.scopeId(),
                    ctx.role(),
                    ctx.displayName(),
                    authoritiesStr,
                    synced
            );
        }
    }

    private static class ContextRowMapper implements RowMapper<IamAccessContext> {
        @Override
        public IamAccessContext mapRow(ResultSet rs, int rowNum) throws SQLException {
            String authStr = rs.getString("authorities");
            Set<String> authorities = new HashSet<>();
            if (authStr != null && !authStr.isBlank()) {
                for (String part : authStr.split(",")) {
                    if (!part.isBlank()) {
                        authorities.add(part.trim());
                    }
                }
            }
            Timestamp synced = rs.getTimestamp("synced_at");
            return new IamAccessContext(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("assignment_id"),
                    rs.getString("scope_key"),
                    rs.getString("scope_id"),
                    rs.getString("role"),
                    rs.getString("display_name"),
                    authorities,
                    synced != null ? synced.toInstant() : null
            );
        }
    }
}
