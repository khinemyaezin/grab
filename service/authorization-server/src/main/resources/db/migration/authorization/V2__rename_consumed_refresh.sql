ALTER TABLE seller_consumed_refresh RENAME TO oauth2_consumed_refresh;
ALTER INDEX idx_seller_consumed_expiry RENAME TO idx_oauth2_consumed_expiry;

CREATE VIEW seller_consumed_refresh AS
SELECT digest, authorization_id, expires_at FROM oauth2_consumed_refresh;
