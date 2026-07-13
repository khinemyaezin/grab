INSERT INTO authorities (code, name, description, active)
VALUES
    ('INVENTORY_READ', 'INVENTORY_READ', 'Ability to view inventory locations, zones, bins, and items', TRUE),
    ('INVENTORY_WRITE', 'INVENTORY_WRITE', 'Ability to manage inventory locations, zones, bins, and items', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO roles (uuid, code, name, description, active, role_kind, assignable)
VALUES
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380301', 'INVENTORY_LOCATION_OPERATOR', 'Inventory Location Operator',
     'Manage inventory within a specific fulfillment location', TRUE, 'SYSTEM', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON (
    role.code = 'INVENTORY_LOCATION_OPERATOR'
    AND authority.code IN ('INVENTORY_READ', 'INVENTORY_WRITE')
) OR (
    role.code = 'MERCHANT_OWNER'
    AND authority.code IN ('INVENTORY_READ', 'INVENTORY_WRITE')
)
ON CONFLICT DO NOTHING;

INSERT INTO platform_roles (uuid, platform_id, role_id, active)
SELECT md5(platform.code || ':' || role.code), platform.id, role.id, TRUE
FROM platforms platform
JOIN roles role ON role.code = 'INVENTORY_LOCATION_OPERATOR'
WHERE platform.code = 'SELLER_PORTAL'
ON CONFLICT (platform_id, role_id) DO NOTHING;

INSERT INTO platform_authorities (platform_id, authority_id)
SELECT platform.id, authority.id
FROM platforms platform
JOIN authorities authority ON (
    platform.code = 'SELLER_PORTAL'
    AND authority.code IN ('INVENTORY_READ', 'INVENTORY_WRITE')
)
ON CONFLICT DO NOTHING;
