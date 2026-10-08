DO $$
BEGIN
    IF EXISTS (SELECT scope_key FROM security_scope_definitions GROUP BY scope_key HAVING COUNT(*) > 1) THEN
        RAISE EXCEPTION 'Security scope ownership collisions must be resolved before migration';
    END IF;
    IF EXISTS (SELECT 1 FROM authorities WHERE category IS NULL OR category NOT IN ('identity', 'merchant', 'catalog', 'inventory', 'saleschannel')) THEN
        RAISE EXCEPTION 'Authority ownership requires a reviewed category backfill before migration';
    END IF;
END $$;

ALTER TABLE security_scope_definitions DROP CONSTRAINT uk_security_scope_module_key;
ALTER TABLE security_scope_definitions ADD CONSTRAINT uk_security_scope_key UNIQUE (scope_key);
ALTER TABLE security_scope_definitions ADD COLUMN provider_lifecycle VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE security_scope_definitions ADD COLUMN source_revision INTEGER NOT NULL DEFAULT 0;
UPDATE security_scope_definitions SET source_revision = manifest_version;
UPDATE security_scope_definitions SET provider_lifecycle = 'RETIRED' WHERE active = FALSE;

DO $$
DECLARE revision_row RECORD;
BEGIN
    FOR revision_row IN SELECT id, payload FROM security_manifest_revision WHERE payload ~ '^[0-9]+$' LOOP
        IF EXISTS (SELECT 1 FROM pg_catalog.pg_largeobject_metadata WHERE oid = revision_row.payload::oid) THEN
            UPDATE security_manifest_revision
            SET payload = convert_from(lo_get(revision_row.payload::oid), 'UTF8')
            WHERE id = revision_row.id;
        END IF;
    END LOOP;
END $$;

ALTER TABLE authorities ADD COLUMN owner_key VARCHAR(128);
UPDATE authorities SET owner_key = category;
ALTER TABLE authorities ALTER COLUMN owner_key SET NOT NULL;
ALTER TABLE authorities ADD COLUMN provider_lifecycle VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE authorities ADD COLUMN source_revision INTEGER NOT NULL DEFAULT 0;

UPDATE authorities a SET source_revision = m.applied_revision
FROM security_manifest_module m WHERE a.owner_key = m.module_key;

UPDATE authorities a SET provider_lifecycle = 'RETIRED'
FROM security_manifest_revision r, jsonb_array_elements(r.payload::jsonb -> 'authorities') d
WHERE r.status = 'APPLIED' AND a.owner_key = r.module_key AND a.code = d ->> 'code'
    AND d ->> 'lifecycle' = 'RETIRED';

ALTER TABLE security_manifest_conflict ADD COLUMN supplied_digest VARCHAR(128);
ALTER TABLE security_manifest_conflict ADD COLUMN payload TEXT;
ALTER TABLE authorities ADD CONSTRAINT ck_authority_provider_lifecycle CHECK (provider_lifecycle IN ('ACTIVE', 'RETIRED'));
ALTER TABLE security_scope_definitions ADD CONSTRAINT ck_scope_provider_lifecycle CHECK (provider_lifecycle IN ('ACTIVE', 'RETIRED'));

CREATE FUNCTION guard_security_manifest_revision_identity() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.module_key <> OLD.module_key OR NEW.revision <> OLD.revision
        OR NEW.content_digest <> OLD.content_digest OR NEW.payload <> OLD.payload
        OR NEW.event_id <> OLD.event_id OR NEW.received_at <> OLD.received_at THEN
        RAISE EXCEPTION 'Canonical security manifest identity is immutable';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER immutable_security_manifest_revision BEFORE UPDATE ON security_manifest_revision
    FOR EACH ROW EXECUTE FUNCTION guard_security_manifest_revision_identity();
