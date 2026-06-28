CREATE TABLE platforms (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE platform_roles (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    platform_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_platform_roles_platform_role UNIQUE (platform_id, role_id),
    CONSTRAINT fk_platform_roles_platform FOREIGN KEY (platform_id) REFERENCES platforms(id),
    CONSTRAINT fk_platform_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE access_assignments (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    platform_role_id BIGINT NOT NULL,
    scope_type VARCHAR(255) NOT NULL,
    scope_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    assigned_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_access_assignment_scope CHECK (
        (scope_type = 'GLOBAL' AND scope_id = '*') OR
        (scope_type <> 'GLOBAL' AND scope_id <> '*')
    ),
    CONSTRAINT fk_access_assignment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_assignment_platform_role FOREIGN KEY (platform_role_id) REFERENCES platform_roles(id)
);

CREATE INDEX idx_access_assignment_user_platform
    ON access_assignments(user_id, platform_role_id);
CREATE INDEX idx_access_assignment_scope
    ON access_assignments(scope_type, scope_id);
CREATE UNIQUE INDEX uk_access_assignment_current
    ON access_assignments(user_id, platform_role_id, scope_type, scope_id)
    WHERE status IN ('ACTIVE', 'SUSPENDED');

CREATE TABLE access_invitations (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    invitee_email VARCHAR(255) NOT NULL,
    platform_role_id BIGINT NOT NULL,
    scope_type VARCHAR(255) NOT NULL,
    scope_id VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    invited_by VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_by VARCHAR(255),
    CONSTRAINT ck_access_invitation_scope CHECK (
        (scope_type = 'GLOBAL' AND scope_id = '*') OR
        (scope_type <> 'GLOBAL' AND scope_id <> '*')
    ),
    CONSTRAINT fk_access_invitation_platform_role FOREIGN KEY (platform_role_id) REFERENCES platform_roles(id)
);

CREATE INDEX idx_access_invitation_email ON access_invitations(invitee_email);
CREATE INDEX idx_access_invitation_scope ON access_invitations(scope_type, scope_id);

ALTER TABLE refresh_sessions ADD COLUMN platform_code VARCHAR(255);
ALTER TABLE refresh_sessions ADD COLUMN assignment_uuid VARCHAR(255);
ALTER TABLE refresh_sessions ADD COLUMN scope_type VARCHAR(255);
ALTER TABLE refresh_sessions ADD COLUMN scope_id VARCHAR(255);

INSERT INTO platforms (uuid, code, name, active)
VALUES
    ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380101', 'CUSTOMER_APP', 'Customer App', TRUE),
    ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380102', 'SELLER_PORTAL', 'Seller Portal', TRUE),
    ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380103', 'ADMIN_CONSOLE', 'Admin Console', TRUE);

INSERT INTO authorities (code, name, description, active)
VALUES
    ('MERCHANT_ONBOARDING_WRITE', 'MERCHANT_ONBOARDING_WRITE', 'Complete merchant onboarding', TRUE),
    ('MERCHANT_READ_OWN', 'MERCHANT_READ_OWN', 'Read an assigned merchant', TRUE),
    ('MERCHANT_WRITE_OWN', 'MERCHANT_WRITE_OWN', 'Manage an assigned merchant', TRUE),
    ('MERCHANT_MEMBER_MANAGE', 'MERCHANT_MEMBER_MANAGE', 'Manage merchant staff access', TRUE),
    ('MERCHANT_REVIEW', 'MERCHANT_REVIEW', 'Review merchant applications', TRUE),
    ('STOREFRONT_WRITE_OWN', 'STOREFRONT_WRITE_OWN', 'Manage an assigned storefront', TRUE),
    ('ORDER_MANAGE_OWN', 'ORDER_MANAGE_OWN', 'Manage orders in an assigned scope', TRUE),
    ('ACCESS_ASSIGNMENT_MANAGE', 'ACCESS_ASSIGNMENT_MANAGE', 'Manage scoped access assignments', TRUE),
    ('ACCESS_INVITATION_MANAGE', 'ACCESS_INVITATION_MANAGE', 'Manage scoped staff invitations', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO roles (uuid, code, name, description, active)
VALUES
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380110', 'MERCHANT_APPLICANT', 'Merchant Applicant', 'Complete onboarding for one merchant', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380111', 'MERCHANT_OWNER', 'Merchant Owner', 'Own and manage one merchant', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380112', 'MERCHANT_ADMIN', 'Merchant Admin', 'Administer one merchant', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380113', 'STOREFRONT_MANAGER', 'Storefront Manager', 'Manage one storefront', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380114', 'CATALOG_MANAGER', 'Catalog Manager', 'Manage catalog in an assigned storefront', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380115', 'INVENTORY_MANAGER', 'Inventory Manager', 'Manage inventory in an assigned scope', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380116', 'ORDER_MANAGER', 'Order Manager', 'Manage orders in an assigned scope', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380117', 'MERCHANT_REVIEWER', 'Merchant Reviewer', 'Review merchant applications', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380118', 'USER_ADMIN', 'User Admin', 'Administer users and scoped access', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code IN ('MERCHANT_ONBOARDING_WRITE')
WHERE role.code = 'MERCHANT_APPLICANT'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code IN (
    'MERCHANT_READ_OWN', 'MERCHANT_WRITE_OWN', 'MERCHANT_MEMBER_MANAGE',
    'STOREFRONT_WRITE_OWN', 'PRODUCT_WRITE_OWN', 'INVENTORY_MANAGE_OWN',
    'ORDER_MANAGE_OWN', 'ACCESS_ASSIGNMENT_MANAGE', 'ACCESS_INVITATION_MANAGE'
)
WHERE role.code = 'MERCHANT_OWNER'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code IN (
    'MERCHANT_READ_OWN', 'MERCHANT_WRITE_OWN', 'MERCHANT_MEMBER_MANAGE',
    'STOREFRONT_WRITE_OWN', 'ACCESS_ASSIGNMENT_MANAGE', 'ACCESS_INVITATION_MANAGE'
)
WHERE role.code = 'MERCHANT_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code IN ('MERCHANT_READ_OWN', 'STOREFRONT_WRITE_OWN')
WHERE role.code = 'STOREFRONT_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code = 'PRODUCT_WRITE_OWN'
WHERE role.code = 'CATALOG_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code = 'INVENTORY_MANAGE_OWN'
WHERE role.code = 'INVENTORY_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code = 'ORDER_MANAGE_OWN'
WHERE role.code = 'ORDER_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code = 'MERCHANT_REVIEW'
WHERE role.code = 'MERCHANT_REVIEWER'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code IN (
    'USER_READ', 'USER_APPROVE', 'USER_SUSPEND', 'ACCESS_ASSIGNMENT_MANAGE', 'ACCESS_INVITATION_MANAGE'
)
WHERE role.code = 'USER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON TRUE
WHERE role.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO platform_roles (uuid, platform_id, role_id, active)
SELECT
    md5(platform.code || ':' || role.code),
    platform.id,
    role.id,
    TRUE
FROM platforms platform
JOIN roles role ON (
    (platform.code = 'CUSTOMER_APP' AND role.code = 'CUSTOMER') OR
    (platform.code = 'SELLER_PORTAL' AND role.code IN (
        'MERCHANT_APPLICANT', 'MERCHANT_OWNER', 'MERCHANT_ADMIN',
        'STOREFRONT_MANAGER', 'CATALOG_MANAGER', 'INVENTORY_MANAGER', 'ORDER_MANAGER'
    )) OR
    (platform.code = 'ADMIN_CONSOLE' AND role.code IN (
        'MERCHANT_REVIEWER', 'USER_ADMIN', 'SUPER_ADMIN'
    ))
)
ON CONFLICT (platform_id, role_id) DO NOTHING;

INSERT INTO access_assignments (
    uuid, user_id, platform_role_id, scope_type, scope_id,
    status, assigned_by, created_at, updated_at, expires_at
)
SELECT
    md5(users.uuid || ':' || platforms.code || ':' || roles.code),
    users.id,
    platform_roles.id,
    'GLOBAL',
    '*',
    'ACTIVE',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL
FROM users
JOIN user_roles ON user_roles.user_id = users.id
JOIN roles ON roles.id = user_roles.role_id
JOIN platforms ON (
    (roles.code = 'CUSTOMER' AND platforms.code = 'CUSTOMER_APP') OR
    (roles.code = 'SUPER_ADMIN' AND platforms.code = 'ADMIN_CONSOLE')
)
JOIN platform_roles ON platform_roles.platform_id = platforms.id
    AND platform_roles.role_id = roles.id
ON CONFLICT DO NOTHING;
