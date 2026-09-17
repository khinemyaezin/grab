CREATE TABLE sales_channels (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    owner VARCHAR(32) NOT NULL,
    merchant_id VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_sales_channel_merchant ON sales_channels(merchant_id);
CREATE INDEX idx_sales_channel_type ON sales_channels(type);
CREATE INDEX idx_sales_channel_status ON sales_channels(status);
CREATE UNIQUE INDEX uk_sales_channel_marketplace
    ON sales_channels(type)
    WHERE type = 'MARKETPLACE';
CREATE UNIQUE INDEX uk_sales_channel_website_merchant
    ON sales_channels(merchant_id)
    WHERE type = 'WEBSITE' AND merchant_id IS NOT NULL;

CREATE TABLE sales_channel_outbox_events (
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

CREATE INDEX idx_sales_channel_outbox_status_available ON sales_channel_outbox_events(status, available_at);
CREATE INDEX idx_sales_channel_outbox_claimed_at ON sales_channel_outbox_events(claimed_at);
CREATE INDEX idx_sales_channel_outbox_aggregate ON sales_channel_outbox_events(aggregate_type, aggregate_id);
