-- Pre-create the per-module publication state row so that concurrent replicas booting against an empty schema never
-- race on the INSERT (the bootstrap lock makes publication single-writer; this removes the PK-violation retry path).
-- Defaults mirror the JPA constructor: revision 0, empty digest, version 0.
INSERT INTO security_manifest_publication (module_key, security_revision, content_digest, version)
VALUES ('inventory', 0, '', 0)
ON CONFLICT (module_key) DO NOTHING;
