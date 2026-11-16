# Outbox, inbox, scheduler & bootstrap metrics

| Metric | Tags | Source | Suggested alert |
|---|---|---|---|
| `outbox.pending` (gauge) | module | `OutboxBacklogGauges` (15s default, `<module>.outbox.metrics.backlog-cache-ttl-ms` over `outbox.metrics.backlog-cache-ttl-ms`) | — |
| `outbox.oldest_pending_age_seconds` (gauge) | module | same | `max by (module) > 60` for 5m |
| `outbox.published` / `outbox.failed` / `outbox.dead` | module, event_type | `MicrometerOutboxObserver` | `increase(outbox_dead_total[5m]) > 0` |
| `outbox.delivery.lag` (timer) | module | same | — |
| `outbox.hot_queue.dropped` | module | `OutboxBacklogGauges` | sustained rate > 0 → raise `<module>.outbox.hot-queue.capacity` |
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

Call the owning module's `outbox.administration().replayDead(fromId, toId, limit)`
from Java. Replay locks up to 1,000 `DEAD` rows in the ID range and moves them to
`NEW` with `attempt_count = 0`. The poller redelivers them, and consumers drop
duplicates through their inbox.

## Scheduling and claim checks

Each module has its own scheduler. Set `<module>.scheduling.pool-size` to change its
two-thread default. Polling uses `<module>.outbox.fixed-delay-ms` (5000) and outbox
cleanup uses `<module>.outbox.cleanup-fixed-delay-ms` (300000). For modules with
an inbox, `<module>.inbox.cleanup.initial-delay-ms` and `<module>.inbox.cleanup.fixed-delay-ms`
override the corresponding `inbox.cleanup.*` values.

Set `<module>.scheduling.lock-provider` to `jdbc` (the default, using database
time), `jdbc-app-time`, or `local` for a single replica. JDBC providers use
the module's existing ShedLock table. The claim strategy is checked at startup
using `<module>.outbox.claim-strategy-check` (`fail`, `warn`, or `off`).
Its fallback is `outbox.claim-strategy-check`, with a default of `fail`.
