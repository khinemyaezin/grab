CREATE INDEX IF NOT EXISTS idx_product_variant_sku
    ON product_variant (sku);

CREATE INDEX IF NOT EXISTS idx_product_publication_sales_channel_variant
    ON product_sales_channel_publication (sales_channel_id, variant_id);
