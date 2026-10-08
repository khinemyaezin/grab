CREATE TABLE IF NOT EXISTS authorities (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_authorities_uuid UNIQUE (uuid)
);

CREATE INDEX IF NOT EXISTS idx_authorities_category ON authorities (category);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    role_kind VARCHAR(32) NOT NULL DEFAULT 'CUSTOM',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    assignable BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_roles_kind CHECK (role_kind IN ('SYSTEM', 'CUSTOM'))
);

CREATE TABLE IF NOT EXISTS role_authorities (
    role_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, authority_id),
    CONSTRAINT fk_role_authorities_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_authorities_authority FOREIGN KEY (authority_id) REFERENCES authorities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS external_identities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    issuer VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    linked_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_external_identities_issuer_subject UNIQUE (issuer, subject),
    CONSTRAINT fk_external_identities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS external_entitlement_mappings (
    id BIGSERIAL PRIMARY KEY,
    issuer VARCHAR(255) NOT NULL,
    entitlement VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT uk_external_entitlement_mapping UNIQUE (issuer, entitlement, role_id),
    CONSTRAINT fk_external_entitlement_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refresh_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    token_family_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    replaced_by_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP,
    assignment_uuid VARCHAR(255),
    scope_key VARCHAR(255),
    scope_id VARCHAR(255),
    CONSTRAINT fk_refresh_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS access_assignments (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    scope_key VARCHAR(255) NOT NULL,
    scope_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    assigned_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_access_assignment_scope CHECK (
        (scope_key = 'global' AND scope_id = '*') OR
        (scope_key <> 'global' AND scope_id <> '*')
    ),
    CONSTRAINT fk_access_assignment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_assignment_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX IF NOT EXISTS idx_access_assignment_user_role
    ON access_assignments (user_id, role_id);
CREATE INDEX IF NOT EXISTS idx_access_assignment_scope
    ON access_assignments (scope_key, scope_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_access_assignment_current
    ON access_assignments (user_id, role_id, scope_key, scope_id)
    WHERE status IN ('ACTIVE', 'SUSPENDED');

CREATE TABLE IF NOT EXISTS access_invitations (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    invitee_email VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    scope_key VARCHAR(255) NOT NULL,
    scope_id VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    invited_by VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_by VARCHAR(255),
    CONSTRAINT ck_access_invitation_scope CHECK (
        (scope_key = 'global' AND scope_id = '*') OR
        (scope_key <> 'global' AND scope_id <> '*')
    ),
    CONSTRAINT fk_access_invitation_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX IF NOT EXISTS idx_access_invitation_email
    ON access_invitations (invitee_email);
CREATE INDEX IF NOT EXISTS idx_access_invitation_scope
    ON access_invitations (scope_key, scope_id);

CREATE TABLE IF NOT EXISTS role_delegation_rules (
    id BIGSERIAL PRIMARY KEY,
    delegator_role_id BIGINT NOT NULL,
    delegated_role_id BIGINT NOT NULL,
    CONSTRAINT uk_role_delegation_rule UNIQUE (delegator_role_id, delegated_role_id),
    CONSTRAINT fk_role_delegation_delegator FOREIGN KEY (delegator_role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_delegation_delegated FOREIGN KEY (delegated_role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS identity_outbox_event (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_version INT NOT NULL,
    headers TEXT NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    occurred_at TIMESTAMP NOT NULL,
    available_at TIMESTAMP NOT NULL,
    claimed_at TIMESTAMP,
    claim_token VARCHAR(255),
    published_at TIMESTAMP,
    last_error TEXT
);

CREATE INDEX IF NOT EXISTS idx_identity_outbox_status_available
    ON identity_outbox_event (status, available_at);
CREATE INDEX IF NOT EXISTS idx_identity_outbox_claimed_at
    ON identity_outbox_event (claimed_at);
CREATE INDEX IF NOT EXISTS idx_identity_outbox_aggregate
    ON identity_outbox_event (aggregate_type, aggregate_id);
CREATE INDEX IF NOT EXISTS idx_identity_outbox_published_at
    ON identity_outbox_event (published_at);
