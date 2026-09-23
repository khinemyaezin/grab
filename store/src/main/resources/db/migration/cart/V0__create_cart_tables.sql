CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    sales_channel_id VARCHAR(255) NOT NULL,
    channel_type VARCHAR(64) NOT NULL,
    region_id VARCHAR(255),
    currency_code VARCHAR(16) NOT NULL,
    guest_token VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255),
    order_id VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    address_line1 VARCHAR(255),
    address_city VARCHAR(255),
    address_country VARCHAR(255),
    address_phone VARCHAR(64),
    address_contact_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cart_guest_channel_region ON carts(guest_token, sales_channel_id, region_id, status);
CREATE INDEX idx_cart_uuid ON carts(uuid);

CREATE TABLE cart_lines (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    variant_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255),
    seller_id VARCHAR(255) NOT NULL,
    title VARCHAR(512) NOT NULL,
    sku VARCHAR(255) NOT NULL,
    unit_price NUMERIC(19, 4) NOT NULL,
    quantity INT NOT NULL
);

CREATE TABLE cart_outbox_events (
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

CREATE INDEX idx_cart_outbox_status_available ON cart_outbox_events(status, available_at);
