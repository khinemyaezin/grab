ALTER TABLE merchant_accounts ADD COLUMN revocation_revision bigint NOT NULL DEFAULT 0;
ALTER TABLE storefronts ADD COLUMN revocation_revision bigint NOT NULL DEFAULT 0;

CREATE FUNCTION increment_scope_revocation_revision() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status AND NEW.status IN ('SUSPENDED', 'CLOSED') THEN
        NEW.revocation_revision := OLD.revocation_revision + 1;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER merchant_scope_revocation
BEFORE UPDATE OF status ON merchant_accounts
FOR EACH ROW EXECUTE FUNCTION increment_scope_revocation_revision();

CREATE TRIGGER storefront_scope_revocation
BEFORE UPDATE OF status ON storefronts
FOR EACH ROW EXECUTE FUNCTION increment_scope_revocation_revision();
