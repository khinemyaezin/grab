CREATE TABLE IF NOT EXISTS product_sales_channel_publication (
    product_id BIGINT NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    sales_channel_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_id, sales_channel_id)
);

CREATE INDEX IF NOT EXISTS idx_product_sales_channel_publication_channel
    ON product_sales_channel_publication (sales_channel_id);
