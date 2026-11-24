# Current outbox implementation review

Reviewed on **2026-11-14**, on `feature/outbox`, with base commit `ea05a342` and the current staged, unstaged, and newly added implementation files. This reviews the working tree, not only the committed implementation or the refactoring proposal.

**Scope:** `framework` outbox contracts, `outbox-infrastructure`, `scheduling-infrastructure`, owner persistence mappings, `app` composition and representative event consumers, workflow integration, migrations, and related tests. **The `store/` module is excluded**, including its wiring, consumers, tests, and compatibility with this refactor. No implementation fixes are included in this review.

The infrastructure has a sound separation of production, claiming, delivery, outcomes, and maintenance. However, the end-to-end delivery contract still has correctness and recovery gaps. The highest-priority issue is that unordered delivery can permanently regress the inventory projection.

## Findings

Priorities: **P1** = fix before production reliance; **P2** = normal corrective work; **P3** = lower-priority operational correctness. Findings below are based on current source behavior; runtime verification is identified separately.

| ID | Priority | Finding |
|---|---|---|
| F1 | P1 | Inventory projections accept stale events and can resurrect deleted variants |
| F2 | P2 | Inbox expiry is shorter than the supported dead-event replay horizon |
| F3 | P2 | A hot/cold collision silently discards an already claimed cold item |
| F4 | P2 | Scheduler shutdown can wait forever before outbox claims are released |
| F5 | P3 | Outcome metrics are recorded before their database transaction commits |

### F1 — Guard projections against stale and repeated events

**Primary location:** [ProjectProductVariantViewService.java, lines 25–50](../inventory-application/src/main/java/com/inventory/application/service/ProjectProductVariantViewService.java#L25).

The relay defaults to [two workers](../outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/module/OutboxModule.java#L65), permits concurrent events for the same aggregate, and mixes hot and cold work. Retries also allow a newer event to overtake an older failed event. Ordering claim results by row ID does not guarantee ordering of consumer commits.

The [inventory listener](../app/src/main/java/com/grab/app/merchandising/inventory/internal/adapter/inbound/event/ProductVariantViewProjectionEventListener.java#L35) forwards payload changes without a source event ID or aggregate revision. The projection service applies them unconditionally. In particular, an `Added` event sets `replacePresentation=true`, and [the policy resets the status to `ACTIVE`](../inventory-application/src/main/java/com/inventory/application/policy/ProductVariantProjectionPolicy.java#L11).

**Failure sequence:** an add event is delayed while a later delete is applied. If the projection does not yet exist, the delete is silently ignored; the delayed add then creates an active projection. Alternatively, after add and delete have both been applied, a redelivered add can reset the existing deleted projection to active. Older SKU/name updates can similarly overwrite newer values. All deliveries can finish as `PUBLISHED` while the projection remains wrong.

**Recommendation:** carry a stable source event ID and a monotonic source revision into the projection command. Apply the revision check and inbox entry atomically with the projection update, preserving deletion tombstones where needed. A source-state refresh design is another option if it prevents stale writes. Inbox deduplication alone does not solve first-time out-of-order events; one worker alone does not solve retries or multiple replicas.

**Regression coverage:** deliver delete before add, deliver update revision 2 before revision 1, and replay add after a later delete. Assert that the final projection matches the latest source state.

### F2 — Align inbox retention with replay eligibility

**Primary location:** [ProcessedEventRetention.java, lines 54–62](../outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/inbox/ProcessedEventRetention.java#L54).

Inbox cleanup deletes deduplication records by age, with a [14-day default](../outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/inbox/ProcessedEventRetention.java#L21). The customer configuration also [defaults to 14 days](../app/src/main/java/com/grab/app/account/customer/internal/config/CustomerModuleOutboxConfig.java#L79). Meanwhile, [replayDead](../outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/core/service/OutboxAdministrationService.java#L34) accepts any matching dead row regardless of age, and outbox retention deletes only published rows. Dead rows therefore remain replayable after their consumers have forgotten them.

**Failure sequence:** a consumer commits its local work, but a later listener or the source publication commit fails. The source event eventually becomes `DEAD`. After inbox cleanup removes the successful consumer's marker, an operator replays the event. That consumer executes again. For example, [RegisterCustomerCommandHandler](../app/src/main/java/com/grab/app/account/customer/internal/command/handler/RegisterCustomerCommandHandler.java#L35) no longer detects the duplicate, and [RegisterCustomerService](../customer-application/src/main/java/com/customer/application/service/RegisterCustomerService.java#L30) throws `AlreadyExists`. Recovery can repeatedly fail even though that consumer completed the original work. Other non-idempotent consumers could repeat side effects.

**Recommendation:** define and enforce a replay horizon that is compatible with every consumer's deduplication retention. If arbitrarily old recovery is required, retain durable deduplication information or make the consumer operation durably idempotent by its business key. The seven-day retention of *published* source rows is not a bound on the lifetime of failed or dead deliveries.

**Regression coverage:** commit one consumer, fail source acknowledgement, expire its inbox entry, and replay the dead source event. Verify either safe recovery or explicit rejection before replay starts.

### F3 — Preserve cold claims when local in-flight acquisition fails

**Primary location:** [DualQueueOutboxRelay.java, lines 153–158](../outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/adapter/in/relay/DualQueueOutboxRelay.java#L153).

The worker removes an item from its queue and simply continues if another worker already holds the same row ID in the in-memory tracker. That is safe for a redundant hot hint, but a cold item already owns a committed database claim.

**Failure sequence:** a hot worker acquires the local tracker and pauses before `claimOne`. The poller claims that row and queues it as cold work. A second worker dequeues the cold item, fails the tracker check, and discards it. The hot worker then sees the row as `PROCESSING` and cannot claim it. Neither worker delivers it; the database row remains claimed until stale-claim recovery, normally two minutes later.

The [delivery engine releases claims rejected during enqueue](../outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/core/service/OutboxDeliveryEngine.java#L74), but it cannot observe this later discard. The discarded claim also cannot be recovered by draining the queue on shutdown.

**Verification:** a temporary two-worker Java harness held a hot handler for ID 1, queued a cold claim for ID 1, then queued ID 2 as a sentinel. After the sentinel ran, the result was `cold handler calls=0, queued cold claims=0`. This directly reproduces the relay discard; the full database timing sequence above was established by source inspection, not a PostgreSQL test.

**Recommendation:** distinguish cold claims from hot hints at this branch. Preserve or reschedule cold work, or return it through a claim-token-aware release path. Do not unconditionally release a claim that could still belong to an active delivery.

**Regression coverage:** coordinate the poller and hot worker with latches so the poller wins the database claim while the hot worker holds the local tracker. Assert delivery or safe release without waiting for lease expiry.

### F4 — Bound scheduler shutdown

**Primary location:** [ModuleJobScheduler.java, lines 91–118](../scheduling-infrastructure/src/main/java/com/grab/scheduling/infrastructure/ModuleJobScheduler.java#L91).

`stop()` cancels scheduled runs with `cancel(false)`, then waits for executor termination using `Long.MAX_VALUE`. Interruption is remembered but does not end the wait. A job blocked on database I/O or a row lock can therefore block shutdown indefinitely.

The scheduler stops before `OutboxModule` because it has the higher lifecycle phase. Consequently, an indefinitely blocked scheduler prevents the outbox module from reaching its bounded worker drain and queued-claim release. The hot-queue drain timeout does not cover this earlier wait. With the hot queue disabled, a synchronous event listener can also hold the polling job open.

**Recommendation:** introduce a configurable scheduler shutdown deadline, bounded database/transaction timeouts, and an explicit timeout path. Preserve the ordering between stopping new claims and draining workers, while accounting for a late-finishing poll so it cannot strand new claims after the drain.

**Regression coverage:** block a scheduled poll, initiate shutdown, and assert bounded return plus safe handling of any claims produced by a late completion. The existing scheduler test releases its latch and proves waiting for a cooperative task, not bounded shutdown of a stuck task.

### F5 — Emit outcome metrics only after a successful commit

**Primary location:** [OutboxOutcomeRecorder.java, lines 50–54](../outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/core/service/OutboxOutcomeRecorder.java#L50).

`observer.published(...)` executes before the surrounding delivery transaction commits. The failed/dead observer callbacks likewise run inside their outcome transactions. A flush failure, deadlock, rollback-only transaction, or commit failure leaves the counter incremented even though the corresponding state transition did not persist.

**Impact:** a delivery can be counted as published and then retried or dead-lettered; dead-event alerts can also count rolled-back changes. These counters cannot reliably represent durable outcomes during database failures.

**Recommendation:** record committed outcomes after transaction success, using an after-commit mechanism or a transaction result that is observed only after successful completion. If attempt metrics are also useful, name and record them separately.

**Regression coverage:** make transaction commit fail after the callback returns and assert that the durable-outcome counter does not advance.

## Current implementation and guarantees

The composition inspected contains nine owner modules in `app` plus workflow infrastructure:

| Owner | Transaction manager used by outbox | Inbox retention wired |
|---|---|---|
| Catalog | `catalogTransactionManager` | Yes |
| Inventory | `inventoryTransactionManager` | Yes |
| Pricing | `pricingTransactionManager` | Yes |
| Identity | `identityTransactionManager` | No |
| Merchant | `merchantTransactionManager` | Yes |
| Customer | `customerTransactionManager` | Yes |
| Sales channel | `salesChannelTransactionManager` | Yes |
| Region | `regionTransactionManager` | No |
| Cart | `cartTransactionManager` | No |
| Workflows | `workflowsTransactionManager` | No generic inbox retention in `WorkflowInfraConfig` |

The absence of generic inbox retention in this table is an inventory observation, not by itself a defect or proof that a module lacks other idempotency mechanisms.

The delivery path is:

1. `JpaOutboxDomainEventProducer` persists and flushes serialized events through the owner's entity manager in the caller's transaction. Each row receives a stable UUID and trace headers.
2. The after-commit hook offers row IDs to the hot queue. A full or failed hot queue falls back to database polling.
3. Polling claims due `NEW`/`FAILED` rows and stale `PROCESSING` rows in a separate short transaction. Both skip-locked and conditional-update strategies use claim tokens.
4. Delivery loads the claimed row, restores event/trace context, and invokes the Spring event publisher synchronously inside a new owner transaction.
5. Success refreshes the row under a blocking lock, checks ownership again, and marks it published. Failures are recorded in another transaction, with bounded retries and poison-event dead lettering.
6. Owner schedulers run competing pollers and singleton maintenance jobs. Administration exposes backlog inspection and dead-row replay.

Useful properties already present:

- The reusable core depends on small ports; JPA, Spring transaction handling, relay queues, and Micrometer are separate adapters. The focused hexagon architecture test passes.
- Claim outcomes explicitly refresh managed entities under lock, avoiding a stale persistence-context claim-token check.
- Retry scheduling, terminal `DEAD` state, replay with the original event ID, and bounded deletion batches are implemented.
- Module composition pairs each outbox entity manager with its owner transaction manager. Claim queries use JPQL/JPA locking rather than native Spring Data queries.
- Inbox insertion and consumer work share a local transaction in the customer/merchant handlers and the workflow step runner inspected.

The contract is **at-least-once delivery with bounded retries**, followed by manual recovery of dead rows. It is neither exactly-once processing nor guaranteed aggregate ordering. A source transaction rollback cannot undo work already committed through another module's transaction manager; consumer idempotency is required even with claim fencing.

`InProcessOutboxEventDispatcher` is a local transport. Current domain-event bridges synchronously publish integration events while handling the persisted source event, so failures can propagate back to that source delivery. Extraction into separate processes still requires a durable transport and suitable event contracts. The dispatcher seam alone does not provide delivery to an absent remote consumer.

## Repository-rule alignment and remaining coverage

- **R17:** the inspected outbox consumers use `@EventListener`, preserving synchronous exception propagation to delivery. This is essential despite delivery itself running asynchronously relative to the original business request. Converting these listeners to `@Async` or after-commit transactional listeners would require a different acknowledgement design.
- **R17/R21 consumer boundaries:** [LocationDeactivationEventListener](../app/src/main/java/com/grab/app/merchandising/inventory/internal/adapter/inbound/event/LocationDeactivationEventListener.java#L21) directly injects a domain repository, contrary to the listener/port boundary rules. Its cascade lookup should be moved behind a destination command/use case. Its catch-and-log behavior also merits failure-path coverage; transactional handler failures may still mark the enclosing transaction rollback-only, so catching them is not proof that the source event is acknowledged successfully.
- **R16/R27 architecture drift:** the supplied invariants describe group-scoped transaction managers and schemas, while the current outbox composition is bounded-context scoped, as the table shows. The owner manager pairing is internally consistent, but it should not be reported as compliance with the documented group topology. Resolve the intended topology explicitly in the architecture rules and matching tests.
- **R22:** [ConditionalUpdateClaimH2Test](../outbox-infrastructure/src/test/java/com/grab/outbox/infrastructure/ConditionalUpdateClaimH2Test.java#L38) still exercises a JPA persistence contract against in-memory H2. PostgreSQL Testcontainers contracts exist for catalog and inventory, but the H2 test does not satisfy the supplied target-database requirement. If portability testing is intentional, document it as supplemental coverage and retain target-database contracts as the required checks.
- PostgreSQL claim/store, replay, singleton-lock, and catalog extraction tests are present. The focused unit suite does not prove real database locking, cross-manager rollback behavior, or migration compatibility. The five failure sequences above need dedicated coverage.

## Validation performed

**Passed:** clean compilation of `scheduling-infrastructure` and its reactor dependencies; 29 focused tests, with zero failures, errors, or skips:

| Test class | Passed |
|---|---:|
| `OutboxDeliveryEngineTest` | 12 |
| `JpaOutboxDomainEventProducerTest` | 7 |
| `OutboxHexagonArchitectureTest` | 1 |
| `DualQueueOutboxRelayTest` | 3 |
| `ModuleJobSchedulerTest` | 3 |

The initial test invocation encountered Mockito self-attachment restrictions. Re-running with the locally installed Mockito JAR supplied as a startup Java agent passed without changing repository configuration:

```sh
JAVA_TOOL_OPTIONS="-javaagent:/path/to/mockito-core-5.17.0.jar" \
mvn -o -pl outbox-infrastructure,scheduling-infrastructure -am \
  -Dtest=OutboxDeliveryEngineTest,DualQueueOutboxRelayTest,JpaOutboxDomainEventProducerTest,OutboxHexagonArchitectureTest,ModuleJobSchedulerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

The temporary collision harness described in F3 also reproduced the discarded cold claim. It was kept outside the repository.

**Not validated:** PostgreSQL/Testcontainers execution, full `app` startup, full application architecture tests, and the full reactor. Docker socket access was denied in this sandbox. No `store` tests were run, and no conclusions about that module's compatibility are made.

## Recommended order of work

1. Protect inventory projections against stale events and duplicate replay (F1).
2. Establish a replay/deduplication retention contract (F2).
3. Preserve cold claims during relay collisions and bound shutdown (F3–F4).
4. Move durable-outcome metrics after commit (F5).
5. Run the PostgreSQL contracts and add the failure-sequence tests before treating the refactor as production-verified; reconcile the rule deviations separately.
