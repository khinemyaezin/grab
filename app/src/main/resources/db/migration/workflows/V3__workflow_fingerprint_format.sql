ALTER TABLE workflow_signal_receipt
    ADD COLUMN outcome_fingerprint varchar(128);

CREATE INDEX idx_workflow_signal_outcome_fingerprint
    ON workflow_signal_receipt (execution_id, logical_operation_id, outcome_fingerprint, fingerprint_version, transport_message_id);

ALTER TABLE workflow_execution
    ADD COLUMN fingerprint_recovery_required boolean NOT NULL DEFAULT false;

UPDATE workflow_execution execution
SET status = 'NEEDS_ATTENTION',
    fingerprint_recovery_required = true,
    failure_reason = 'Workflow fingerprint recovery required before execution can resume',
    version = version + 1
WHERE status NOT IN ('COMPLETED', 'CANCELLED', 'COMPENSATED', 'FAILED')
  AND EXISTS (
      SELECT 1 FROM workflow_start_receipt receipt
      WHERE receipt.execution_id = execution.id AND receipt.fingerprint_version < 2
  );

ALTER TABLE workflow_execution
    ADD COLUMN transition_revision bigint NOT NULL DEFAULT 0;
