CREATE TABLE merchant_accounts (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    applicant_user_id VARCHAR(255) NOT NULL,
    merchant_type VARCHAR(32) NOT NULL,
    legal_name VARCHAR(255),
    display_name VARCHAR(255) NOT NULL,
    registration_country_code VARCHAR(2),
    registration_number VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(64),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    address_city VARCHAR(128),
    address_region VARCHAR(128),
    address_postal_code VARCHAR(32),
    address_country_code VARCHAR(2),
    status VARCHAR(32) NOT NULL,
    lifecycle_reason VARCHAR(1000),
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_merchant_registration_pair CHECK (
        (registration_country_code IS NULL AND registration_number IS NULL) OR
        (registration_country_code IS NOT NULL AND registration_number IS NOT NULL)
    )
);

CREATE INDEX idx_merchant_applicant ON merchant_accounts(applicant_user_id);
CREATE INDEX idx_merchant_status ON merchant_accounts(status);
CREATE UNIQUE INDEX uk_merchant_open_application
    ON merchant_accounts(applicant_user_id, merchant_type)
    WHERE status IN ('DRAFT', 'PENDING_REVIEW', 'CHANGES_REQUESTED', 'ACTIVE', 'SUSPENDED');
CREATE UNIQUE INDEX uk_merchant_business_registration
    ON merchant_accounts(registration_country_code, registration_number)
    WHERE registration_country_code IS NOT NULL AND registration_number IS NOT NULL;

CREATE TABLE merchant_outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_version INTEGER NOT NULL,
    headers TEXT NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    occurred_at TIMESTAMP NOT NULL,
    available_at TIMESTAMP NOT NULL,
    claimed_at TIMESTAMP,
    claim_token VARCHAR(255),
    published_at TIMESTAMP,
    last_error TEXT
);

CREATE INDEX idx_merchant_outbox_status_available ON merchant_outbox_events(status, available_at);
CREATE INDEX idx_merchant_outbox_claimed_at ON merchant_outbox_events(claimed_at);
CREATE INDEX idx_merchant_outbox_aggregate ON merchant_outbox_events(aggregate_type, aggregate_id);
