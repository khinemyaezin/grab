ALTER TABLE access_assignments
    ADD COLUMN suspension_source VARCHAR(64);

UPDATE access_assignments
SET suspension_source = 'MANUAL'
WHERE status = 'SUSPENDED';

ALTER TABLE access_assignments
    ADD CONSTRAINT ck_access_assignment_suspension_source CHECK (
        (status = 'SUSPENDED' AND suspension_source IN ('MANUAL', 'RESOURCE_LIFECYCLE')) OR
        (status <> 'SUSPENDED' AND suspension_source IS NULL)
    );

CREATE TABLE identity_inbox_events (
    event_id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    aggregate_version BIGINT NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_identity_inbox_merchant_version
    ON identity_inbox_events(merchant_id, aggregate_version);

INSERT INTO authorities (code, name, description, active)
VALUES (
    'MERCHANT_APPLICATION_CREATE',
    'MERCHANT_APPLICATION_CREATE',
    'Start a merchant application',
    TRUE
)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code = 'MERCHANT_APPLICATION_CREATE'
WHERE role.code = 'CUSTOMER'
ON CONFLICT DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.code = 'MERCHANT_APPLICATION_CREATE'
WHERE role.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;
