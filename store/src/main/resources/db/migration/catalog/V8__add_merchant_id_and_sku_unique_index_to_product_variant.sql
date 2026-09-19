ALTER TABLE product_variant ADD COLUMN IF NOT EXISTS merchant_id VARCHAR(255);

UPDATE product_variant pv
SET merchant_id = p.merchant_id
FROM product p
WHERE pv.product_id = p.id AND pv.merchant_id IS NULL;

ALTER TABLE product_variant ALTER COLUMN merchant_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_product_variant_merchant_sku 
ON product_variant (merchant_id, LOWER(sku));
