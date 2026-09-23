ALTER TABLE platform_roles ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;

-- Set default roles for existing platforms
UPDATE platform_roles
SET is_default = TRUE
WHERE (platform_id, role_id) IN (
    SELECT p.id, r.id
    FROM platforms p
    JOIN roles r ON (p.code = 'CUSTOMER_APP' AND r.code = 'CUSTOMER')
);

-- Ensure at most one default role per platform
CREATE UNIQUE INDEX uk_platform_roles_default
    ON platform_roles (platform_id)
    WHERE is_default = TRUE;
