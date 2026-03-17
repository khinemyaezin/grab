INSERT INTO variant_type (uuid, code, name)
VALUES
    ('seed-vt-color', 'COLOR', 'Color'),
    ('seed-vt-size', 'SIZE', 'Size')
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'seed-vo-color-red', 'COLOR_RED', 'Red', vt.id
FROM variant_type vt
WHERE vt.code = 'COLOR'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'seed-vo-color-blue', 'COLOR_BLUE', 'Blue', vt.id
FROM variant_type vt
WHERE vt.code = 'COLOR'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'seed-vo-color-black', 'COLOR_BLACK', 'Black', vt.id
FROM variant_type vt
WHERE vt.code = 'COLOR'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'seed-vo-size-small', 'SIZE_SMALL', 'Small', vt.id
FROM variant_type vt
WHERE vt.code = 'SIZE'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'seed-vo-size-medium', 'SIZE_MEDIUM', 'Medium', vt.id
FROM variant_type vt
WHERE vt.code = 'SIZE'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'seed-vo-size-large', 'SIZE_LARGE', 'Large', vt.id
FROM variant_type vt
WHERE vt.code = 'SIZE'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO category (uuid, name, lft, rgt, depth, active, listing_allowed, review_required, c2c_allowed)
SELECT *
FROM (
    VALUES
        ('seed-cat-electronics', 'Electronics', 1, 6, 0, true, true, false, true),
        ('seed-cat-smartphones', 'Smartphones', 2, 3, 1, true, true, false, true),
        ('seed-cat-laptops', 'Laptops', 4, 5, 1, true, true, false, true),
        ('seed-cat-fashion', 'Fashion', 7, 10, 0, true, true, false, true),
        ('seed-cat-men-fashion', 'Men''s Fashion', 8, 9, 1, true, true, false, true)
) AS seed(uuid, name, lft, rgt, depth, active, listing_allowed, review_required, c2c_allowed)
WHERE NOT EXISTS (SELECT 1 FROM category);
