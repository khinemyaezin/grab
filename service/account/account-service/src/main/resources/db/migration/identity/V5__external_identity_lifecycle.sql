ALTER TABLE users
    ADD COLUMN identity_management_mode varchar(32) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE users
    ADD CONSTRAINT ck_users_identity_management_mode
        CHECK (identity_management_mode IN ('LOCAL', 'EXTERNAL'));

ALTER TABLE external_identities
    ADD COLUMN directory_id varchar(255),
    ADD COLUMN resource_id varchar(255),
    ADD COLUMN linked_by varchar(255),
    ADD COLUMN updated_by varchar(255),
    ADD COLUMN enabled boolean NOT NULL DEFAULT true,
    ADD COLUMN directory_revision bigint NOT NULL DEFAULT 0,
    ADD COLUMN directory_observed_at timestamp(6);

CREATE UNIQUE INDEX uq_external_identity_directory_resource
    ON external_identities (directory_id, resource_id)
    WHERE directory_id IS NOT NULL AND resource_id IS NOT NULL;

CREATE INDEX idx_external_identity_directory_enabled
    ON external_identities (directory_id, enabled);
