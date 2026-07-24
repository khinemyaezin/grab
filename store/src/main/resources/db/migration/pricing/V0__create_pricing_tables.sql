CREATE TABLE price_sets (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE price_lists (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE,
    ends_at TIMESTAMP WITH TIME ZONE,
    rules_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE prices (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255),
    currency_code VARCHAR(16) NOT NULL,
    amount NUMERIC(19, 6) NOT NULL,
    min_quantity INTEGER,
    max_quantity INTEGER,
    rules_count INTEGER NOT NULL DEFAULT 0,
    price_set_id BIGINT NOT NULL REFERENCES price_sets(id) ON DELETE CASCADE,
    price_list_id BIGINT REFERENCES price_lists(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_prices_price_set_id ON prices(price_set_id);
CREATE INDEX idx_prices_price_list_id ON prices(price_list_id);
CREATE INDEX idx_prices_currency_code ON prices(currency_code);

CREATE TABLE price_rules (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    price_id BIGINT NOT NULL REFERENCES prices(id) ON DELETE CASCADE,
    attribute VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    operator VARCHAR(16) NOT NULL DEFAULT 'EQ',
    priority INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_price_rules_price_id ON price_rules(price_id);
CREATE INDEX idx_price_rules_attribute ON price_rules(price_id, attribute);

CREATE TABLE price_list_rules (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    price_list_id BIGINT NOT NULL REFERENCES price_lists(id) ON DELETE CASCADE,
    attribute VARCHAR(255) NOT NULL,
    values_json TEXT NOT NULL
);

CREATE INDEX idx_price_list_rules_price_list_id ON price_list_rules(price_list_id);

CREATE TABLE price_preferences (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    attribute VARCHAR(255) NOT NULL,
    value VARCHAR(255),
    is_tax_inclusive BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_price_preferences_attribute_value ON price_preferences(attribute, value);

CREATE TABLE pricing_outbox_events (
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

CREATE INDEX idx_pricing_outbox_status_available ON pricing_outbox_events(status, available_at);
CREATE INDEX idx_pricing_outbox_claimed_at ON pricing_outbox_events(claimed_at);
CREATE INDEX idx_pricing_outbox_aggregate ON pricing_outbox_events(aggregate_type, aggregate_id);
