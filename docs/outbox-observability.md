# Outbox, inbox, scheduler & bootstrap metrics (plan Phase 5)

| Metric | Tags | Source | Suggested alert |
|---|---|---|---|
| `outbox.pending` (gauge) | module | `OutboxBacklogMetrics` (cached 15s, `outbox.metrics.backlog-cache-ttl-ms`) | — |
| `outbox.oldest_pending_age_seconds` (gauge) | module | same | `max by (module) > 60` for 5m |
| `outbox.published` / `outbox.failed` / `outbox.dead` | module, event_type | `MicrometerOutboxObserver` | `increase(outbox_dead_total[5m]) > 0` |
| `outbox.delivery.lag` (timer) | module | same | — |
| `outbox.hot_queue.dropped` | module | `OutboxBacklogMetrics` | sustained rate > 0 → raise `<module>.outbox.hot-queue.capacity` |
| `outbox.claim.lost` / `outbox.claim.released` / `outbox.cleanup.deleted` | module | `MicrometerOutboxObserver` | `claim.lost` > 0 → claim timeout too short |
| `inbox.duplicate` | consumer | `ObservedProcessedEventRepository` | informational |
| `scheduler.job.duration` (outcome) / `scheduler.job.skipped_locked` | job | `ShedLockSingletonJobExecutor` | duration > lockAtMostFor / 2 |
| `bootstrap.task.status` (1/0) / `bootstrap.task.duration` | task, required | `BootstrapMetrics` | `bootstrap_task_status{required="true"} == 0` for 10m |
| `security.manifest.publication.result` | module, kind (`manifest`/`role`), result | `ObservedPublicationPorts` | `result="CONFLICT"` > 0 |

The backlog gauges are table-wide, so every replica reports the same value. Aggregate them with `max`, not `sum`.

## Log context

Listener logs carry `traceId` (taken from the event headers) and `eventId` (the outbox event id) through the MDC.
`logback-spring.xml` prints both.

## Dead-letter replay

`OutboxReplayer.replayDead(module, fromId, toId, limit)` locks the `DEAD` rows in the id range and moves them to
`NEW` with `attempt_count = 0`, at most 1000 per call. The poller then redelivers them, and consumers drop any
duplicates through their inbox. The replayer is not exposed over HTTP yet: an ops-only permission has to be defined
first.
