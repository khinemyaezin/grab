DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM access_assignments
        WHERE scope_type NOT IN ('GLOBAL', 'MERCHANT_ACCOUNT', 'STOREFRONT', 'FULFILLMENT_LOCATION')
        UNION ALL
        SELECT 1 FROM access_invitations
        WHERE scope_type NOT IN ('GLOBAL', 'MERCHANT_ACCOUNT', 'STOREFRONT', 'FULFILLMENT_LOCATION')
        UNION ALL
        SELECT 1 FROM refresh_sessions
        WHERE scope_type IS NOT NULL
          AND scope_type NOT IN ('GLOBAL', 'MERCHANT_ACCOUNT', 'STOREFRONT', 'FULFILLMENT_LOCATION')
    ) THEN
        RAISE EXCEPTION 'Unknown legacy identity scope type; scope-key migration stopped';
    END IF;
END $$;

ALTER TABLE access_assignments DROP CONSTRAINT ck_access_assignment_scope;
ALTER TABLE access_invitations DROP CONSTRAINT ck_access_invitation_scope;

ALTER TABLE access_assignments RENAME COLUMN scope_type TO scope_key;
ALTER TABLE access_invitations RENAME COLUMN scope_type TO scope_key;
ALTER TABLE refresh_sessions RENAME COLUMN scope_type TO scope_key;

UPDATE access_assignments
SET scope_key = CASE scope_key
    WHEN 'GLOBAL' THEN 'global'
    WHEN 'MERCHANT_ACCOUNT' THEN 'merchant.account'
    WHEN 'STOREFRONT' THEN 'merchant.storefront'
    WHEN 'FULFILLMENT_LOCATION' THEN 'inventory.fulfillment-location'
END;

UPDATE access_invitations
SET scope_key = CASE scope_key
    WHEN 'GLOBAL' THEN 'global'
    WHEN 'MERCHANT_ACCOUNT' THEN 'merchant.account'
    WHEN 'STOREFRONT' THEN 'merchant.storefront'
    WHEN 'FULFILLMENT_LOCATION' THEN 'inventory.fulfillment-location'
END;

UPDATE refresh_sessions
SET scope_key = CASE scope_key
    WHEN 'GLOBAL' THEN 'global'
    WHEN 'MERCHANT_ACCOUNT' THEN 'merchant.account'
    WHEN 'STOREFRONT' THEN 'merchant.storefront'
    WHEN 'FULFILLMENT_LOCATION' THEN 'inventory.fulfillment-location'
END
WHERE scope_key IS NOT NULL;

ALTER TABLE access_assignments ADD CONSTRAINT ck_access_assignment_scope CHECK (
    scope_key ~ '^(global|[a-z][a-z0-9-]*([.][a-z][a-z0-9-]*)+)$'
    AND (
        (scope_key = 'global' AND scope_id = '*') OR
        (scope_key <> 'global' AND scope_id <> '*')
    )
);

ALTER TABLE access_invitations ADD CONSTRAINT ck_access_invitation_scope CHECK (
    scope_key ~ '^(global|[a-z][a-z0-9-]*([.][a-z][a-z0-9-]*)+)$'
    AND (
        (scope_key = 'global' AND scope_id = '*') OR
        (scope_key <> 'global' AND scope_id <> '*')
    )
);

ALTER TABLE refresh_sessions ADD CONSTRAINT ck_refresh_session_scope CHECK (
    (scope_key IS NULL AND scope_id IS NULL) OR
    (
        scope_key ~ '^(global|[a-z][a-z0-9-]*([.][a-z][a-z0-9-]*)+)$'
        AND (
            (scope_key = 'global' AND scope_id = '*') OR
            (scope_key <> 'global' AND scope_id <> '*')
        )
    )
);

CREATE TABLE role_delegation_rules (
    id BIGSERIAL PRIMARY KEY,
    delegator_role_id BIGINT NOT NULL,
    delegated_role_id BIGINT NOT NULL,
    CONSTRAINT uk_role_delegation_rule UNIQUE (delegator_role_id, delegated_role_id),
    CONSTRAINT fk_role_delegation_delegator FOREIGN KEY (delegator_role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_delegation_delegated FOREIGN KEY (delegated_role_id) REFERENCES roles(id) ON DELETE CASCADE
);

WITH configured_rules(delegator_code, delegated_code) AS (
    VALUES
        ('MERCHANT_OWNER', 'MERCHANT_ADMIN'),
        ('MERCHANT_OWNER', 'STOREFRONT_MANAGER'),
        ('MERCHANT_OWNER', 'CATALOG_MANAGER'),
        ('MERCHANT_OWNER', 'INVENTORY_MANAGER'),
        ('MERCHANT_OWNER', 'ORDER_MANAGER'),
        ('MERCHANT_ADMIN', 'STOREFRONT_MANAGER'),
        ('MERCHANT_ADMIN', 'CATALOG_MANAGER'),
        ('MERCHANT_ADMIN', 'INVENTORY_MANAGER'),
        ('MERCHANT_ADMIN', 'ORDER_MANAGER')
)
INSERT INTO role_delegation_rules (delegator_role_id, delegated_role_id)
SELECT delegator.id, delegated.id
FROM configured_rules rule
JOIN roles delegator ON delegator.code = rule.delegator_code
JOIN roles delegated ON delegated.code = rule.delegated_code
ON CONFLICT DO NOTHING;

INSERT INTO role_delegation_rules (delegator_role_id, delegated_role_id)
SELECT DISTINCT delegator.id, delegated.id
FROM roles delegator
CROSS JOIN platform_roles supported_role
JOIN roles delegated ON delegated.id = supported_role.role_id
WHERE delegator.code IN ('SUPER_ADMIN', 'USER_ADMIN')
  AND delegator.active = TRUE
  AND supported_role.active = TRUE
  AND delegated.active = TRUE
ON CONFLICT DO NOTHING;
