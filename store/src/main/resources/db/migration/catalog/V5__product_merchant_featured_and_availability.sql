ALTER TABLE product
    ADD COLUMN IF NOT EXISTS merchant_id VARCHAR(255);

UPDATE product
SET merchant_id = uuid
WHERE merchant_id IS NULL
  AND uuid IS NOT NULL;

ALTER TABLE product
    ALTER COLUMN merchant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_product_merchant ON product (merchant_id);

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS featured BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

UPDATE product
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW())
WHERE created_at IS NULL
   OR updated_at IS NULL;

ALTER TABLE product
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE product
    ALTER COLUMN updated_at SET NOT NULL;

CREATE TABLE IF NOT EXISTS catalog_merchant_availability (
    merchant_id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    merchant_type VARCHAR(32),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
