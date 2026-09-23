INSERT INTO authorities (code, name, description, active)
VALUES
    ('MERCHANT_STOREFRONT_READ', 'MERCHANT_STOREFRONT_READ', 'Ability to view merchant storefronts', TRUE),
    ('MERCHANT_STOREFRONT_WRITE', 'MERCHANT_STOREFRONT_WRITE', 'Ability to manage merchant storefronts', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON (
    role.code = 'MERCHANT_ADMIN'
    AND authority.code IN ('MERCHANT_STOREFRONT_READ', 'MERCHANT_STOREFRONT_WRITE')
)
ON CONFLICT DO NOTHING;

INSERT INTO platform_authorities (platform_id, authority_id)
SELECT platform.id, authority.id
FROM platforms platform
JOIN authorities authority ON (
    platform.code = 'SELLER_PORTAL'
    AND authority.code IN ('MERCHANT_STOREFRONT_READ', 'MERCHANT_STOREFRONT_WRITE')
) OR (
    platform.code = 'ADMIN_CONSOLE'
    AND authority.code IN ('MERCHANT_STOREFRONT_READ')
)
ON CONFLICT DO NOTHING;
