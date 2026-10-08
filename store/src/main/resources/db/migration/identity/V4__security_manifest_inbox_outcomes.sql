ALTER TABLE security_manifest_inbox
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'APPLIED';

ALTER TABLE security_manifest_inbox
    ADD COLUMN error_code VARCHAR(128);

CREATE INDEX IF NOT EXISTS idx_security_manifest_inbox_module_revision
    ON security_manifest_inbox (module_key, revision);
