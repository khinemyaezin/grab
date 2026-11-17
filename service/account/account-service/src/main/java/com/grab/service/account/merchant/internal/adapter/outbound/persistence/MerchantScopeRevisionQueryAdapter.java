package com.grab.service.account.merchant.internal.adapter.outbound.persistence;

import com.merchant.application.model.read.MerchantScopeRevision;
import com.merchant.application.model.read.ResolveMerchantScopeRevisionQuery;
import com.merchant.application.port.outbound.MerchantScopeRevisionQueryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Component
public final class MerchantScopeRevisionQueryAdapter implements MerchantScopeRevisionQueryPort {
    private static final RowMapper<MerchantScopeRevision> REVISION = (result, row) ->
            new MerchantScopeRevision(result.getLong(1), result.getLong(2));

    private final JdbcTemplate jdbc;
    private final String merchantQuery;
    private final String storefrontQuery;

    public MerchantScopeRevisionQueryAdapter(@Qualifier("accountDataSource") DataSource dataSource,
                                             @Value("${merchant.flyway.default-schema:merchant}") String schema) {
        if (!schema.matches("[a-z_][a-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid Merchant schema identifier");
        }
        jdbc = new JdbcTemplate(dataSource);
        merchantQuery = """
                SELECT revocation_revision, 0
                FROM %s.merchant_accounts
                WHERE uuid = ? AND status = 'ACTIVE'
                """.formatted(schema);
        storefrontQuery = """
                SELECT m.revocation_revision, s.revocation_revision
                FROM %s.storefronts s JOIN %s.merchant_accounts m ON m.uuid = s.merchant_id
                WHERE s.uuid = ? AND m.status = 'ACTIVE' AND s.status = 'ACTIVE'
                """.formatted(schema, schema);
    }

    @Override
    public MerchantScopeRevision find(ResolveMerchantScopeRevisionQuery query) {
        String sql = switch (query.scopeKey()) {
            case "merchant.account" -> merchantQuery;
            case "merchant.storefront" -> storefrontQuery;
            default -> null;
        };
        if (sql == null) {
            return null;
        }
        String id = query.scopeId().getValue();
        List<MerchantScopeRevision> rows = jdbc.query(sql, REVISION, id);
        return rows.isEmpty() ? null : rows.getFirst();
    }
}
