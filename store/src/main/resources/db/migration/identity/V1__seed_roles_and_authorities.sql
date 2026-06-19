-- Insert Authorities
INSERT INTO authorities (code, name, description, active)
VALUES
    ('USER_APPROVE', 'USER_APPROVE', 'Ability to approve user accounts', true),
    ('USER_READ', 'USER_READ', 'Ability to read user details', true),
    ('USER_SUSPEND', 'USER_SUSPEND', 'Ability to suspend users', true),
    ('ROLE_MANAGE', 'ROLE_MANAGE', 'Ability to manage roles', true),
    ('PRODUCT_WRITE_OWN', 'PRODUCT_WRITE_OWN', 'Ability to write own products', true),
    ('INVENTORY_MANAGE_OWN', 'INVENTORY_MANAGE_OWN', 'Ability to manage own inventory', true)
ON CONFLICT (code) DO NOTHING;

-- Insert Roles
INSERT INTO roles (uuid, code, name, description, active)
VALUES
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01', 'CUSTOMER', 'CUSTOMER', 'Customer role', true),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380b02', 'SELLER', 'SELLER', 'Seller role', true),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380b03', 'ADMIN', 'ADMIN', 'Admin role', true)
ON CONFLICT (code) DO NOTHING;

-- Insert Role Authorities mappings
INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id FROM roles r, authorities a
WHERE r.code = 'SELLER' AND a.code IN ('PRODUCT_WRITE_OWN', 'INVENTORY_MANAGE_OWN')
ON CONFLICT DO NOTHING;

-- ADMIN gets SELLER_APPROVE, USER_READ, USER_SUSPEND, ROLE_MANAGE
INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id FROM roles r, authorities a
WHERE r.code = 'ADMIN' AND a.code IN ('USER_APPROVE', 'USER_READ', 'USER_SUSPEND', 'ROLE_MANAGE')
ON CONFLICT DO NOTHING;
