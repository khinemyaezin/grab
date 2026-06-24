DO $$
BEGIN
    IF '${seedAdmin}' = 'true' THEN
        IF NOT EXISTS (SELECT 1 FROM users WHERE email = '${adminEmail}') THEN
            INSERT INTO users (uuid, email, password_hash, status, created_at, updated_at)
            VALUES (gen_random_uuid(), '${adminEmail}', '${adminPasswordHash}', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        END IF;

        INSERT INTO user_roles (user_id, role_id)
        SELECT u.id, r.id FROM users u, roles r
        WHERE u.email = '${adminEmail}' AND r.code = 'SUPER_ADMIN'
        ON CONFLICT DO NOTHING;
    END IF;
END $$;
