-- Local Development Seed: Separate Domestic Regions & Countries across Southeast Asia & Asia
INSERT INTO regions (id, name, currency_code, status)
VALUES
    ('reg_sg', 'Singapore', 'SGD', 'ACTIVE'),
    ('reg_my', 'Malaysia', 'MYR', 'ACTIVE'),
    ('reg_th', 'Thailand', 'THB', 'ACTIVE'),
    ('reg_id', 'Indonesia', 'IDR', 'ACTIVE'),
    ('reg_ph', 'Philippines', 'PHP', 'ACTIVE'),
    ('reg_vn', 'Vietnam', 'VND', 'ACTIVE'),
    ('reg_mm', 'Myanmar', 'MMK', 'ACTIVE'),
    ('reg_kh', 'Cambodia', 'KHR', 'ACTIVE'),
    ('reg_la', 'Laos', 'LAK', 'ACTIVE'),
    ('reg_bn', 'Brunei', 'BND', 'ACTIVE'),
    ('reg_jp', 'Japan', 'JPY', 'ACTIVE'),
    ('reg_kr', 'South Korea', 'KRW', 'ACTIVE'),
    ('reg_hk', 'Hong Kong', 'HKD', 'ACTIVE'),
    ('reg_tw', 'Taiwan', 'TWD', 'ACTIVE'),
    ('reg_in', 'India', 'INR', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO region_countries (id, region_id, country_code, created_at)
VALUES
    ('rc_sg', 'reg_sg', 'SG', CURRENT_TIMESTAMP),
    ('rc_my', 'reg_my', 'MY', CURRENT_TIMESTAMP),
    ('rc_th', 'reg_th', 'TH', CURRENT_TIMESTAMP),
    ('rc_id', 'reg_id', 'ID', CURRENT_TIMESTAMP),
    ('rc_ph', 'reg_ph', 'PH', CURRENT_TIMESTAMP),
    ('rc_vn', 'reg_vn', 'VN', CURRENT_TIMESTAMP),
    ('rc_mm', 'reg_mm', 'MM', CURRENT_TIMESTAMP),
    ('rc_kh', 'reg_kh', 'KH', CURRENT_TIMESTAMP),
    ('rc_la', 'reg_la', 'LA', CURRENT_TIMESTAMP),
    ('rc_bn', 'reg_bn', 'BN', CURRENT_TIMESTAMP),
    ('rc_jp', 'reg_jp', 'JP', CURRENT_TIMESTAMP),
    ('rc_kr', 'reg_kr', 'KR', CURRENT_TIMESTAMP),
    ('rc_hk', 'reg_hk', 'HK', CURRENT_TIMESTAMP),
    ('rc_tw', 'reg_tw', 'TW', CURRENT_TIMESTAMP),
    ('rc_in', 'reg_in', 'IN', CURRENT_TIMESTAMP)
ON CONFLICT (country_code) DO NOTHING;
