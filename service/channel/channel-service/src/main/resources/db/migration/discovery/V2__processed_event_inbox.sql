-- Consumer-side inbox (scheduling plan 2.2). One row per (consumer, outbox event_id) applied in this schema.
-- Written in the same transaction as the consumer's side effect; redeliveries see the row and skip.
CREATE TABLE IF NOT EXISTS processed_event (
    consumer     varchar(150)             NOT NULL,
    event_id     uuid                     NOT NULL,
    processed_at timestamp(6) with time zone NOT NULL DEFAULT now(),
    CONSTRAINT pk_processed_event PRIMARY KEY (consumer, event_id)
);

CREATE INDEX IF NOT EXISTS idx_processed_event_processed_at ON processed_event (processed_at);

CREATE TABLE IF NOT EXISTS shedlock (
    name       varchar(64)  NOT NULL,
    lock_until timestamp(3) NOT NULL,
    locked_at  timestamp(3) NOT NULL,
    locked_by  varchar(255) NOT NULL,
    PRIMARY KEY (name)
);
