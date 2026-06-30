-- Insert Authorities
INSERT INTO authorities (code, name, description, active)
VALUES
    ('USER_READ', 'USER_READ', 'Ability to read user details', true),
    ('USER_WRITE', 'USER_WRITE', 'Ability to manage and modify users', true),
    ('ROLE_READ', 'ROLE_READ', 'Ability to read roles', true),
    ('ROLE_WRITE', 'ROLE_WRITE', 'Ability to manage and modify roles', true),
    ('ACCESS_ASSIGNMENT_READ', 'ACCESS_ASSIGNMENT_READ', 'Ability to read scoped access assignments', TRUE),
    ('ACCESS_ASSIGNMENT_WRITE', 'ACCESS_ASSIGNMENT_WRITE', 'Ability to manage scoped access assignments', TRUE),
    ('ACCESS_INVITATION_WRITE', 'ACCESS_INVITATION_WRITE', 'Ability to manage scoped staff invitations', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Insert Roles
INSERT INTO roles (uuid, code, name, description, active)
VALUES
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01', 'CUSTOMER', 'CUSTOMER', 'Customer role', true),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380b02', 'SELLER', 'SELLER', 'Seller role', true),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380118', 'USER_ADMIN', 'User Admin', 'Administer users and scoped access', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Seed Role Authorities
INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r, authorities a
WHERE (r.code = 'USER_ADMIN' AND a.code IN ('USER_READ', 'USER_WRITE', 'ROLE_READ', 'ROLE_WRITE', 'ACCESS_ASSIGNMENT_READ', 'ACCESS_ASSIGNMENT_WRITE', 'ACCESS_INVITATION_WRITE'))
   OR (r.code = 'SELLER' AND a.code IN ('ACCESS_ASSIGNMENT_READ', 'ACCESS_ASSIGNMENT_WRITE', 'ACCESS_INVITATION_WRITE'))
ON CONFLICT DO NOTHING;
