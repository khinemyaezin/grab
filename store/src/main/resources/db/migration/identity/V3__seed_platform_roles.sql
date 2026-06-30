INSERT INTO platforms (uuid, code, name, active)
VALUES ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380101', 'CUSTOMER_APP', 'Customer App', TRUE),
       ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380102', 'SELLER_PORTAL', 'Seller Portal', TRUE),
       ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380103', 'ADMIN_CONSOLE', 'Admin Console', TRUE) ON CONFLICT (code) DO NOTHING;

-- Seed Data for Platform Roles
INSERT INTO platform_roles (uuid, platform_id, role_id, active)
SELECT md5(platform.code || ':' || role.code),
       platform.id,
       role.id,
       TRUE
FROM platforms platform
         JOIN roles role ON (
    (platform.code = 'CUSTOMER_APP' AND role.code IN (
                                                      'CUSTOMER', 'USER_ADMIN'
        )) OR
    (platform.code = 'SELLER_PORTAL' AND role.code IN (
                                                       'SELLER', 'CUSTOMER', 'USER_ADMIN'
        )) OR
    (platform.code = 'ADMIN_CONSOLE' AND role.code IN (
                                                       'USER_ADMIN'
        ))
    ) ON CONFLICT (platform_id, role_id) DO NOTHING;

-- Seed Data for Access Assignments
INSERT INTO access_assignments (uuid, user_id, platform_role_id, scope_key, scope_id,
                                status, assigned_by, created_at, updated_at, expires_at)
SELECT md5(users.uuid || ':' || platforms.code || ':' || roles.code),
       users.id,
       platform_roles.id,
       'global',
       '*',
       'ACTIVE',
       NULL,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       NULL
FROM users
         CROSS JOIN platform_roles
         JOIN platforms ON platforms.id = platform_roles.platform_id
         JOIN roles ON roles.id = platform_roles.role_id
WHERE users.email = '${adminEmail}' -- Assigning all platform roles to the admin user
ON CONFLICT DO NOTHING;

-- Seed Data for Role Delegation Rules
WITH configured_rules(delegator_code, delegated_code) AS (VALUES ('USER_ADMIN', 'SELLER'),
                                                                 ('USER_ADMIN', 'CUSTOMER'))
INSERT
INTO role_delegation_rules (delegator_role_id, delegated_role_id)
SELECT delegator.id, delegated.id
FROM configured_rules rule
         JOIN roles delegator ON delegator.code = rule.delegator_code
         JOIN roles delegated ON delegated.code = rule.delegated_code ON CONFLICT DO NOTHING;

INSERT INTO role_delegation_rules (delegator_role_id, delegated_role_id)
SELECT DISTINCT delegator.id, delegated.id
FROM roles delegator
         CROSS JOIN platform_roles supported_role
         JOIN roles delegated ON delegated.id = supported_role.role_id
WHERE delegator.code IN ('USER_ADMIN')
  AND delegator.active = TRUE
  AND supported_role.active = TRUE
  AND delegated.active = TRUE ON CONFLICT DO NOTHING;
