CREATE TABLE IF NOT EXISTS merchant_view (
    scope_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

DO $$
BEGIN
    IF '${seedDemo}' = 'true' THEN
        IF NOT EXISTS (SELECT 1 FROM users WHERE email = '${demoEmail}') THEN
            INSERT INTO users (uuid, email, password_hash, status, created_at, updated_at)
            VALUES (
                'a0000000-0000-4000-8000-000000000001',
                '${demoEmail}',
                '${demoPasswordHash}',
                'ACTIVE',
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END IF;

        INSERT INTO access_assignments (
            uuid, user_id, platform_role_id, scope_key, scope_id,
            status, assigned_by, created_at, updated_at, expires_at
        )
        SELECT
            'a0000000-0000-4000-8000-000000000011',
            users.id,
            platform_roles.id,
            'merchant.account',
            'a0000000-0000-4000-8000-000000000002',
            'ACTIVE',
            NULL,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP,
            NULL
        FROM users
                 CROSS JOIN platform_roles
                 JOIN platforms ON platforms.id = platform_roles.platform_id
                 JOIN roles ON roles.id = platform_roles.role_id
        WHERE users.email = '${demoEmail}'
          AND platforms.code = 'SELLER_PORTAL'
          AND roles.code = 'MERCHANT_ADMIN'
          AND NOT EXISTS (
              SELECT 1
              FROM access_assignments existing
              WHERE existing.user_id = users.id
                AND existing.platform_role_id = platform_roles.id
                AND existing.scope_key = 'merchant.account'
                AND existing.scope_id = 'a0000000-0000-4000-8000-000000000002'
                AND existing.status IN ('ACTIVE', 'SUSPENDED')
          );

        IF NOT EXISTS (
            SELECT 1 FROM merchant_view WHERE scope_id = 'a0000000-0000-4000-8000-000000000002'
        ) THEN
            INSERT INTO merchant_view (scope_id, name, status, created_at, updated_at)
            VALUES (
                'a0000000-0000-4000-8000-000000000002',
                'Demo Merchant',
                'ACTIVE',
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END IF;
    END IF;
END $$;
