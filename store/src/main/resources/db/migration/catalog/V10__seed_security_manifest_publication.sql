INSERT INTO security_manifest_publication (module_key, security_revision, content_digest, row_version)
VALUES ('catalog', 0, '', 0)
ON CONFLICT (module_key) DO NOTHING;

CREATE FUNCTION guard_security_manifest_publication() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.security_revision < OLD.security_revision
        OR (NEW.security_revision = OLD.security_revision AND NEW.content_digest <> OLD.content_digest) THEN
        RAISE EXCEPTION 'Security publication revision and digest cannot regress or conflict';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER immutable_security_publication BEFORE UPDATE ON security_manifest_publication
    FOR EACH ROW EXECUTE FUNCTION guard_security_manifest_publication();
