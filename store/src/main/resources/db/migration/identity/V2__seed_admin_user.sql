DO $$
BEGIN
    IF '${seedAdmin}' = 'true' THEN
        IF NOT EXISTS (SELECT 1 FROM users WHERE email = '${adminEmail}') THEN
            INSERT INTO users (uuid, email, password_hash, status, created_at, updated_at)
            VALUES (gen_random_uuid(), '${adminEmail}', '${adminPasswordHash}', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        END IF;
    END IF;
END $$;
