INSERT INTO roles (uuid, code, name, description, active)
VALUES
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380201', 'MERCHANT_APPLICANT', 'Merchant Applicant',
     'Complete onboarding for one merchant account', TRUE),
    ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380202', 'MERCHANT_OWNER', 'Merchant Owner',
     'Own and administer one merchant account', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON (
    role.code = 'MERCHANT_APPLICANT'
    AND authority.code IN (
        'MERCHANT_APPLICATION_WRITE',
        'MERCHANT_PROFILE_WRITE',
        'MERCHANT_PROFILE_READ'
    )
) OR (
    role.code = 'MERCHANT_OWNER'
    AND authority.code IN (
        'ACCESS_ASSIGNMENT_READ',
        'ACCESS_ASSIGNMENT_WRITE',
        'ACCESS_INVITATION_WRITE',
        'MERCHANT_APPLICATION_WRITE',
        'MERCHANT_PROFILE_WRITE',
        'MERCHANT_PROFILE_READ'
    )
)
ON CONFLICT DO NOTHING;

INSERT INTO platform_roles (uuid, platform_id, role_id, active)
SELECT md5(platform.code || ':' || role.code), platform.id, role.id, TRUE
FROM platforms platform
JOIN roles role ON role.code IN ('MERCHANT_APPLICANT', 'MERCHANT_OWNER')
WHERE platform.code = 'SELLER_PORTAL'
ON CONFLICT (platform_id, role_id) DO NOTHING;
