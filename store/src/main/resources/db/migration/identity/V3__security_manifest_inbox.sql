CREATE TABLE IF NOT EXISTS security_manifest_inbox (
    event_id VARCHAR(255) PRIMARY KEY,
    module_key VARCHAR(128) NOT NULL,
    revision INTEGER NOT NULL,
    content_digest VARCHAR(128) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
