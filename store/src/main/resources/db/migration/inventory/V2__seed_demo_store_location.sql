CREATE TABLE IF NOT EXISTS location (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    street VARCHAR(255),
    street2 VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    postal_code VARCHAR(255),
    country VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_location_code ON location (code);
CREATE INDEX IF NOT EXISTS idx_location_type ON location (type);

INSERT INTO location (
    uuid,
    code,
    name,
    merchant_id,
    type,
    street,
    street2,
    city,
    state,
    postal_code,
    country,
    active,
    created_at,
    updated_at
)
SELECT
    'a0000000-0000-4000-8000-000000000003',
    'DEMO-STORE-01',
    'Demo Store',
    'a0000000-0000-4000-8000-000000000002',
    'STORE',
    '1 Demo Street',
    NULL,
    'Singapore',
    'Singapore',
    '018956',
    'SG',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM location WHERE uuid = 'a0000000-0000-4000-8000-000000000003'
);
