INSERT INTO category (uuid, name, lft, rgt, depth)
VALUES
    ('e8f9-1234-5678-9012', 'Electronics', 1, 10, 0), -- Root category
    ('bc67-5678-1234-5678', 'Phones', 2, 3, 1),      -- Child of Electronics
    ('a123-7890-5678-2345', 'Laptops', 4, 7, 1),     -- Child of Electronics
    ('f765-5678-2345-7890', 'Tablets', 5, 6, 2);