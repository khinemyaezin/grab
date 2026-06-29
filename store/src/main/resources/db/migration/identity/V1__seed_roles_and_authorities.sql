-- Insert Authorities
INSERT INTO authorities (code, name, description, active)
VALUES
    ('USER_APPROVE', 'USER_APPROVE', 'Ability to approve user accounts', true),
    ('USER_READ', 'USER_READ', 'Ability to read user details', true),
    ('USER_SUSPEND', 'USER_SUSPEND', 'Ability to suspend users', true),
    ('ROLE_MANAGE', 'ROLE_MANAGE', 'Ability to manage roles', true),
    ('PRODUCT_WRITE_OWN', 'PRODUCT_WRITE_OWN', 'Ability to write own products', true),
    ('INVENTORY_MANAGE_OWN', 'INVENTORY_MANAGE_OWN', 'Ability to manage own inventory', true),
    ('MERCHANT_ONBOARDING_WRITE', 'MERCHANT_ONBOARDING_WRITE', 'Complete merchant onboarding', TRUE),
    ('MERCHANT_READ_OWN', 'MERCHANT_READ_OWN', 'Read an assigned merchant', TRUE),
    ('MERCHANT_WRITE_OWN', 'MERCHANT_WRITE_OWN', 'Manage an assigned merchant', TRUE),
    ('MERCHANT_MEMBER_MANAGE', 'MERCHANT_MEMBER_MANAGE', 'Manage merchant staff access', TRUE),
    ('MERCHANT_REVIEW', 'MERCHANT_REVIEW', 'Review merchant applications', TRUE),
    ('STOREFRONT_WRITE_OWN', 'STOREFRONT_WRITE_OWN', 'Manage an assigned storefront', TRUE),
    ('ORDER_MANAGE_OWN', 'ORDER_MANAGE_OWN', 'Manage orders in an assigned scope', TRUE),
    ('ACCESS_ASSIGNMENT_MANAGE', 'ACCESS_ASSIGNMENT_MANAGE', 'Manage scoped access assignments', TRUE),
    ('ACCESS_INVITATION_MANAGE', 'ACCESS_INVITATION_MANAGE', 'Manage scoped staff invitations', TRUE),
    ('MERCHANT_APPLICATION_CREATE', 'MERCHANT_APPLICATION_CREATE', 'Start a merchant application', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Insert Roles
INSERT INTO roles (uuid, code, name, description, active)
VALUES
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01', 'CUSTOMER', 'CUSTOMER', 'Customer role', true),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380b02', 'SELLER', 'SELLER', 'Seller role', true),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'SUPER_ADMIN', 'SUPER_ADMIN', 'Super admin role that bypasses everything', true),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380110', 'MERCHANT_APPLICANT', 'Merchant Applicant', 'Complete onboarding for one merchant', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380111', 'MERCHANT_OWNER', 'Merchant Owner', 'Own and manage one merchant', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380112', 'MERCHANT_ADMIN', 'Merchant Admin', 'Administer one merchant', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380113', 'STOREFRONT_MANAGER', 'Storefront Manager', 'Manage one storefront', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380114', 'CATALOG_MANAGER', 'Catalog Manager', 'Manage catalog in an assigned storefront', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380115', 'INVENTORY_MANAGER', 'Inventory Manager', 'Manage inventory in an assigned scope', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380116', 'ORDER_MANAGER', 'Order Manager', 'Manage orders in an assigned scope', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380117', 'MERCHANT_REVIEWER', 'Merchant Reviewer', 'Review merchant applications', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380118', 'USER_ADMIN', 'User Admin', 'Administer users and scoped access', TRUE)
ON CONFLICT (code) DO NOTHING;
