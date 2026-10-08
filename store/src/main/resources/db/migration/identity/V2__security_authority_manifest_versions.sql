CREATE TABLE IF NOT EXISTS security_authority_manifest_versions (
    id BIGSERIAL PRIMARY KEY,
    module_key VARCHAR(128) NOT NULL UNIQUE,
    manifest_version INTEGER NOT NULL,
    content_digest VARCHAR(128) NOT NULL,
    row_version BIGINT NOT NULL DEFAULT 0
);
