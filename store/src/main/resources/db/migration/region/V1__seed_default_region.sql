INSERT INTO regions (id, name, currency_code, status)
VALUES ('default', 'Default Region', 'MMK', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO region_countries (id, region_id, country_code)
VALUES ('default-MM', 'default', 'MM')
ON CONFLICT (id) DO NOTHING;
