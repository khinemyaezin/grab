ALTER TABLE security_manifest_revision ADD COLUMN IF NOT EXISTS event_id VARCHAR(255);
ALTER TABLE security_manifest_revision ADD COLUMN IF NOT EXISTS payload TEXT NOT NULL DEFAULT '{}';

UPDATE security_manifest_revision
SET event_id = module_key || ':' || revision
WHERE event_id IS NULL;

ALTER TABLE security_manifest_revision ALTER COLUMN event_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_security_manifest_revision_status
    ON security_manifest_revision (status, received_at);
