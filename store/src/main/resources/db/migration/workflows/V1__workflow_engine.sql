ALTER TABLE workflow_instance
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_workflow_instance_status_updated
    ON workflow_instance (status, updated_at);

CREATE TABLE workflow_outbox_events (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(255) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    event_type      VARCHAR(255) NOT NULL,
    event_version   INTEGER      NOT NULL,
    headers         TEXT         NOT NULL,
    payload         TEXT         NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    attempt_count   INTEGER      NOT NULL DEFAULT 0,
    occurred_at     TIMESTAMP    NOT NULL,
    available_at    TIMESTAMP    NOT NULL,
    claimed_at      TIMESTAMP,
    claim_token     VARCHAR(255),
    published_at    TIMESTAMP,
    last_error      TEXT
);

CREATE INDEX idx_workflow_outbox_status_available
    ON workflow_outbox_events (status, available_at);
CREATE INDEX idx_workflow_outbox_claimed_at
    ON workflow_outbox_events (claimed_at);
CREATE INDEX idx_workflow_outbox_aggregate
    ON workflow_outbox_events (aggregate_type, aggregate_id);

CREATE TABLE workflow_correlation (
    workflow_name   VARCHAR(128) NOT NULL,
    correlation_key VARCHAR(256) NOT NULL,
    workflow_id     VARCHAR(64)  NOT NULL,
    step            VARCHAR(128) NOT NULL,
    PRIMARY KEY (workflow_name, correlation_key)
);

CREATE INDEX idx_workflow_correlation_workflow_id
    ON workflow_correlation (workflow_id);

CREATE TABLE workflow_signal_log (
    workflow_id  VARCHAR(64)  NOT NULL,
    step         VARCHAR(128) NOT NULL,
    dedup_key    VARCHAR(256) NOT NULL,
    recorded_at  TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (workflow_id, step, dedup_key)
);
