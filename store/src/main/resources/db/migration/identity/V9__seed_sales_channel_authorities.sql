INSERT INTO authorities (code, name, description, active)
VALUES
    ('SALES_CHANNEL_READ', 'SALES_CHANNEL_READ', 'Ability to view sales channels', TRUE),
    ('SALES_CHANNEL_WRITE', 'SALES_CHANNEL_WRITE', 'Ability to enable or disable seller-owned sales channels', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON (
    role.code = 'MERCHANT_OWNER'
    AND authority.code IN ('SALES_CHANNEL_READ', 'SALES_CHANNEL_WRITE')
)
ON CONFLICT DO NOTHING;

INSERT INTO platform_authorities (platform_id, authority_id)
SELECT platform.id, authority.id
FROM platforms platform
JOIN authorities authority ON (
    platform.code = 'SELLER_PORTAL'
    AND authority.code IN ('SALES_CHANNEL_READ', 'SALES_CHANNEL_WRITE')
) OR (
    platform.code = 'ADMIN_CONSOLE'
    AND authority.code IN ('SALES_CHANNEL_READ')
)
ON CONFLICT DO NOTHING;
