CREATE TABLE IF NOT EXISTS security_scope_definitions (
    id BIGSERIAL PRIMARY KEY,
    module_key VARCHAR(128) NOT NULL,
    scope_key VARCHAR(255) NOT NULL,
    parent_scope_key VARCHAR(255),
    manifest_version INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    row_version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_security_scope_module_key UNIQUE (module_key, scope_key)
);

CREATE INDEX IF NOT EXISTS idx_security_scope_key ON security_scope_definitions (scope_key);
CREATE INDEX IF NOT EXISTS idx_security_scope_module_version
    ON security_scope_definitions (module_key, manifest_version);
