INSERT INTO authorities (code, name, description, active)
VALUES
    -- User & Role Administration
    ('USER_READ', 'USER_READ', 'Ability to read user details', TRUE),
    ('USER_WRITE', 'USER_WRITE', 'Ability to manage and modify users', TRUE),
    ('ROLE_READ', 'ROLE_READ', 'Ability to read roles', TRUE),
    ('ROLE_WRITE', 'ROLE_WRITE', 'Ability to manage and modify roles', TRUE),

    -- Access Control & Delegations
    ('ACCESS_ASSIGNMENT_READ', 'ACCESS_ASSIGNMENT_READ', 'Ability to read scoped access assignments', TRUE),
    ('ACCESS_ASSIGNMENT_WRITE', 'ACCESS_ASSIGNMENT_WRITE', 'Ability to manage scoped access assignments', TRUE),
    ('ACCESS_INVITATION_WRITE', 'ACCESS_INVITATION_WRITE', 'Ability to manage scoped staff invitations', TRUE),

    -- Merchant Account & Management
    ('MERCHANT_GLOBAL_READ', 'MERCHANT_GLOBAL_READ', 'Ability to view all merchants globally', TRUE),
    ('MERCHANT_LIFECYCLE_WRITE', 'MERCHANT_LIFECYCLE_WRITE', 'Ability to change merchant lifecycle status', TRUE),
    ('MERCHANT_APPLICATION_WRITE', 'MERCHANT_APPLICATION_WRITE', 'Ability to submit merchant applications', TRUE),
    ('MERCHANT_PROFILE_WRITE', 'MERCHANT_PROFILE_WRITE', 'Ability to update merchant profile details', TRUE),
    ('MERCHANT_PROFILE_READ', 'MERCHANT_PROFILE_READ', 'Ability to view own merchant profile', TRUE),
    ('MERCHANT_STOREFRONT_READ', 'MERCHANT_STOREFRONT_READ', 'Ability to view merchant storefronts', TRUE),
    ('MERCHANT_STOREFRONT_WRITE', 'MERCHANT_STOREFRONT_WRITE', 'Ability to manage merchant storefronts', TRUE),

    -- Inventory Operations
    ('INVENTORY_READ', 'INVENTORY_READ', 'Ability to view inventory locations, zones, bins, and items', TRUE),
    ('INVENTORY_WRITE', 'INVENTORY_WRITE', 'Ability to manage inventory locations, zones, bins, and items', TRUE),

    -- Sales Channel Operations
    ('SALES_CHANNEL_READ', 'SALES_CHANNEL_READ', 'Ability to view sales channels', TRUE),
    ('SALES_CHANNEL_WRITE', 'SALES_CHANNEL_WRITE', 'Ability to enable or disable seller-owned sales channels', TRUE),

    -- Catalog Operations
    ('CATALOG_READ', 'CATALOG_READ', 'Ability to view catalog items', TRUE),
    ('CATALOG_WRITE', 'CATALOG_WRITE', 'Ability to modify catalog items', TRUE)
ON CONFLICT (code) DO NOTHING;
