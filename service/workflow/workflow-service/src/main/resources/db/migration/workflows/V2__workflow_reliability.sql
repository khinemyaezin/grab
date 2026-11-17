ALTER TABLE workflow_signal_receipt
    ADD COLUMN fingerprint_version integer NOT NULL DEFAULT 1;

ALTER TABLE workflow_start_receipt
    ADD COLUMN fingerprint_version integer NOT NULL DEFAULT 1;

ALTER TABLE workflow_signal_receipt
    DROP CONSTRAINT IF EXISTS uq_workflow_signal_operation_fingerprint;

CREATE INDEX IF NOT EXISTS idx_workflow_signal_operation_fingerprint
    ON workflow_signal_receipt (execution_id, logical_operation_id, payload_fingerprint);

ALTER TABLE workflow_timer
    ADD COLUMN claim_token varchar(128),
    ADD COLUMN lease_expires_at timestamp(6) with time zone;

CREATE INDEX idx_workflow_timer_claim_lease
    ON workflow_timer (status, lease_expires_at);
