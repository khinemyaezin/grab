CREATE TABLE IF NOT EXISTS security_manifest_publication (
    module_key VARCHAR(128) PRIMARY KEY,
    security_revision INTEGER NOT NULL,
    content_digest VARCHAR(128) NOT NULL,
    last_enqueued_at TIMESTAMP WITH TIME ZONE,
    lease_until TIMESTAMP WITH TIME ZONE,
    row_version BIGINT NOT NULL DEFAULT 0
);
