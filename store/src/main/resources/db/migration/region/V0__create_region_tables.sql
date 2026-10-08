CREATE TABLE regions (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL
);

CREATE TABLE region_countries (
    id VARCHAR(96) PRIMARY KEY,
    region_id VARCHAR(64) NOT NULL REFERENCES regions (id),
    country_code VARCHAR(2) NOT NULL,
    CONSTRAINT uq_region_country UNIQUE (region_id, country_code)
);

CREATE INDEX idx_region_countries_region ON region_countries (region_id);

CREATE TABLE region_outbox_events (
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

CREATE INDEX idx_region_outbox_status_available ON region_outbox_events (status, available_at);
CREATE INDEX idx_region_outbox_claimed_at ON region_outbox_events (claimed_at);
CREATE INDEX idx_region_outbox_aggregate ON region_outbox_events (aggregate_type, aggregate_id);
