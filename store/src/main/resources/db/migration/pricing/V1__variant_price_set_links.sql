CREATE TABLE variant_price_set_links (
    variant_id   VARCHAR(64)  PRIMARY KEY,
    price_set_id VARCHAR(64)  NOT NULL,
    product_id   VARCHAR(64)  NOT NULL,
    sku          VARCHAR(128) NOT NULL,
    merchant_id  VARCHAR(64)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_variant_price_set_links_product_id
    ON variant_price_set_links (product_id);

CREATE INDEX idx_variant_price_set_links_price_set_id
    ON variant_price_set_links (price_set_id);

CREATE INDEX idx_variant_price_set_links_merchant_id
    ON variant_price_set_links (merchant_id);
