-- Outbox hardening and scheduler locks (docs/outbox-events-scheduling-plan.md, Phase 1 and 3.1).

-- 1.4 Stable event id, used by consumers for inbox de-duplication.
-- The column default keeps rows written by pods still running the previous release valid during a rolling deploy.
ALTER TABLE workflow_outbox_events ADD COLUMN IF NOT EXISTS event_id uuid;
UPDATE workflow_outbox_events SET event_id = gen_random_uuid() WHERE event_id IS NULL;
ALTER TABLE workflow_outbox_events ALTER COLUMN event_id SET DEFAULT gen_random_uuid();
ALTER TABLE workflow_outbox_events ALTER COLUMN event_id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_workflow_outbox_event_id ON workflow_outbox_events (event_id);

-- 1.5 Bounded retries: terminal DEAD status.
ALTER TABLE workflow_outbox_events DROP CONSTRAINT IF EXISTS workflow_outbox_events_status_check;
ALTER TABLE workflow_outbox_events ADD CONSTRAINT workflow_outbox_events_status_check
    CHECK (status IN ('NEW', 'PROCESSING', 'PUBLISHED', 'FAILED', 'DEAD'));
CREATE INDEX IF NOT EXISTS idx_workflow_outbox_dead ON workflow_outbox_events (id) WHERE status = 'DEAD';

-- 1.6 Zone-independent timestamps. Existing values are interpreted as UTC.
ALTER TABLE workflow_outbox_events
    ALTER COLUMN occurred_at TYPE timestamp(6) with time zone USING occurred_at AT TIME ZONE 'UTC',
    ALTER COLUMN available_at TYPE timestamp(6) with time zone USING available_at AT TIME ZONE 'UTC',
    ALTER COLUMN claimed_at TYPE timestamp(6) with time zone USING claimed_at AT TIME ZONE 'UTC',
    ALTER COLUMN published_at TYPE timestamp(6) with time zone USING published_at AT TIME ZONE 'UTC';

-- 3.1 ShedLock table for singleton jobs owned by this schema (JdbcTemplateLockProvider, usingDbTime()).
CREATE TABLE IF NOT EXISTS shedlock (
    name       varchar(64)  NOT NULL,
    lock_until timestamp(3) NOT NULL,
    locked_at  timestamp(3) NOT NULL,
    locked_by  varchar(255) NOT NULL,
    PRIMARY KEY (name)
);
