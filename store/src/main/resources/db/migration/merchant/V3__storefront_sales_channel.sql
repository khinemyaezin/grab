ALTER TABLE storefronts
    ADD COLUMN sales_channel_id VARCHAR(255);

CREATE INDEX idx_storefront_sales_channel ON storefronts(sales_channel_id);

UPDATE storefronts
SET sales_channel_id = 'c0000000-0000-4000-8000-000000000002'
WHERE merchant_id = 'a0000000-0000-4000-8000-000000000002'
  AND sales_channel_id IS NULL;
