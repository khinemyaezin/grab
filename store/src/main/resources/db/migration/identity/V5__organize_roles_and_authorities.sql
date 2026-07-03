ALTER TABLE roles ADD COLUMN role_kind VARCHAR(32) NOT NULL DEFAULT 'CUSTOM';
ALTER TABLE roles ADD COLUMN assignable BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE roles
SET role_kind = 'SYSTEM'
WHERE code IN ('CUSTOMER', 'USER_ADMIN', 'MERCHANT_APPLICANT', 'MERCHANT_OWNER');

ALTER TABLE roles
    ADD CONSTRAINT ck_roles_kind CHECK (role_kind IN ('SYSTEM', 'CUSTOM'));

CREATE TABLE platform_authorities (
    platform_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    PRIMARY KEY (platform_id, authority_id),
    CONSTRAINT fk_platform_authority_platform
        FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE,
    CONSTRAINT fk_platform_authority_authority
        FOREIGN KEY (authority_id) REFERENCES authorities(id) ON DELETE CASCADE
);

INSERT INTO platform_authorities (platform_id, authority_id)
SELECT platform.id, authority.id
FROM platforms platform
JOIN authorities authority ON (
    platform.code = 'CUSTOMER_APP'
    AND authority.code IN ('MERCHANT_APPLICATION_WRITE')
) OR (
    platform.code = 'SELLER_PORTAL'
    AND authority.code IN (
        'ACCESS_ASSIGNMENT_READ',
        'ACCESS_ASSIGNMENT_WRITE',
        'ACCESS_INVITATION_WRITE',
        'MERCHANT_APPLICATION_WRITE',
        'MERCHANT_PROFILE_WRITE',
        'MERCHANT_PROFILE_READ'
    )
) OR (
    platform.code = 'ADMIN_CONSOLE'
    AND authority.code IN (
        'USER_READ',
        'USER_WRITE',
        'ROLE_READ',
        'ROLE_WRITE',
        'ACCESS_ASSIGNMENT_READ',
        'ACCESS_ASSIGNMENT_WRITE',
        'ACCESS_INVITATION_WRITE',
        'MERCHANT_GLOBAL_READ',
        'MERCHANT_LIFECYCLE_WRITE',
        'MERCHANT_PROFILE_READ'
    )
)
ON CONFLICT DO NOTHING;

INSERT INTO platform_authorities (platform_id, authority_id)
SELECT DISTINCT platform_role.platform_id, role_authority.authority_id
FROM platform_roles platform_role
JOIN roles role ON role.id = platform_role.role_id AND role.role_kind = 'CUSTOM'
JOIN role_authorities role_authority ON role_authority.role_id = role.id
ON CONFLICT DO NOTHING;

UPDATE platform_roles platform_role
SET active = FALSE
FROM platforms platform, roles role
WHERE platform_role.platform_id = platform.id
  AND platform_role.role_id = role.id
  AND (
      (role.code = 'CUSTOMER' AND platform.code <> 'CUSTOMER_APP')
      OR (role.code = 'USER_ADMIN' AND platform.code <> 'ADMIN_CONSOLE')
      OR (role.code IN ('MERCHANT_APPLICANT', 'MERCHANT_OWNER') AND platform.code <> 'SELLER_PORTAL')
  );

UPDATE roles role
SET active = FALSE
WHERE role.role_kind = 'CUSTOM'
  AND NOT EXISTS (
      SELECT 1
      FROM platform_roles platform_role
      WHERE platform_role.role_id = role.id
        AND platform_role.active = TRUE
  );
