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
