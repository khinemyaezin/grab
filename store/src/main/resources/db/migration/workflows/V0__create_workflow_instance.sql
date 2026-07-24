CREATE TABLE workflow_instance (
    id                VARCHAR(64)  PRIMARY KEY,
    workflow_name     VARCHAR(128) NOT NULL,
    status            VARCHAR(32)  NOT NULL,
    current_step      VARCHAR(128),
    correlation_id    VARCHAR(128) NOT NULL,
    idempotency_key   VARCHAR(128),
    context_json      TEXT,
    checkpoint_json   TEXT,
    error_message     TEXT,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);

CREATE UNIQUE INDEX uq_workflow_instance_idempotency
    ON workflow_instance (workflow_name, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX idx_workflow_instance_status
    ON workflow_instance (status);

CREATE INDEX idx_workflow_instance_correlation
    ON workflow_instance (correlation_id);
