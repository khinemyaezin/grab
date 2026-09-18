INSERT INTO sales_channels (
    uuid,
    name,
    type,
    owner,
    merchant_id,
    status,
    created_at,
    updated_at,
    version
)
SELECT
    'c0000000-0000-4000-8000-000000000001',
    'Marketplace',
    'MARKETPLACE',
    'PLATFORM',
    NULL,
    'ENABLED',
    NOW(),
    NOW(),
    0
WHERE NOT EXISTS (
    SELECT 1 FROM sales_channels WHERE type = 'MARKETPLACE'
);

INSERT INTO sales_channels (
    uuid,
    name,
    type,
    owner,
    merchant_id,
    status,
    created_at,
    updated_at,
    version
)
SELECT
    'c0000000-0000-4000-8000-000000000002',
    'Demo Merchant website',
    'WEBSITE',
    'SELLER',
    'a0000000-0000-4000-8000-000000000002',
    'ENABLED',
    NOW(),
    NOW(),
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM sales_channels
    WHERE type = 'WEBSITE'
      AND merchant_id = 'a0000000-0000-4000-8000-000000000002'
);
