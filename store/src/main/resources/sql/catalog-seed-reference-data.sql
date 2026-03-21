INSERT INTO variant_type (uuid, code, name)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'COLOR', 'Color'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'SIZE', 'Size')
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'COLOR_RED', 'Red', vt.id
FROM variant_type vt
WHERE vt.code = 'COLOR'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'COLOR_BLUE', 'Blue', vt.id
FROM variant_type vt
WHERE vt.code = 'COLOR'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'COLOR_BLACK', 'Black', vt.id
FROM variant_type vt
WHERE vt.code = 'COLOR'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'SIZE_SMALL', 'Small', vt.id
FROM variant_type vt
WHERE vt.code = 'SIZE'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'SIZE_MEDIUM', 'Medium', vt.id
FROM variant_type vt
WHERE vt.code = 'SIZE'
ON CONFLICT (code) DO UPDATE
SET uuid = EXCLUDED.uuid,
    name = EXCLUDED.name,
    variant_type_id = EXCLUDED.variant_type_id;

INSERT INTO variant_option (uuid, code, name, variant_type_id)
SELECT 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'SIZE_LARGE', 'Large', vt.id
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
        ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'Root', 1, 24, 0, true, false, false, false),
        ('d1eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'Electronics', 2, 9, 1, true, false, false, false),
        ('e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'Smartphones', 3, 4, 2, true, true, false, true),
        ('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Laptops', 5, 6, 2, true, true, false, true),
        ('a2eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'Accessories', 7, 8, 2, true, true, false, true),
        ('b2eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'Fashion', 10, 17, 1, true, false, false, false),
        ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 'Men''s Fashion', 11, 12, 2, true, true, false, true),
        ('d2eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'Women''s Fashion', 13, 14, 2, true, true, false, true),
        ('e2eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'Kids'' Fashion', 15, 16, 2, true, true, false, true),
        ('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'Home & Living', 18, 23, 1, true, false, false, false),
        ('a3eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'Furniture', 19, 20, 2, true, true, false, true),
        ('b3eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'Kitchen', 21, 22, 2, true, true, false, true)
) AS seed(uuid, name, lft, rgt, depth, active, listing_allowed, review_required, c2c_allowed)
WHERE NOT EXISTS (SELECT 1 FROM category);
