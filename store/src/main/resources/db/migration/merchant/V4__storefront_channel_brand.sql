DROP INDEX IF EXISTS idx_storefront_sales_channel;

CREATE TABLE storefront_channel_brand (
    storefront_id BIGINT NOT NULL,
    sales_channel_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (storefront_id),
    CONSTRAINT fk_storefront_channel_brand_storefront
        FOREIGN KEY (storefront_id) REFERENCES storefronts (id)
);

INSERT INTO storefront_channel_brand (storefront_id, sales_channel_id)
SELECT id, sales_channel_id
FROM storefronts
WHERE sales_channel_id IS NOT NULL;

ALTER TABLE storefronts DROP COLUMN sales_channel_id;
