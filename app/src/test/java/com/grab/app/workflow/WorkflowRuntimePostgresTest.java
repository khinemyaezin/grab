package com.grab.app.workflow;

import com.grab.app.workflow.internal.adapter.inbound.message.WorkflowRuntimeCommandPortAdapter;
import com.grab.app.workflow.internal.adapter.inbound.message.WorkflowRuntimeSignalPortAdapter;
import com.grab.app.workflow.internal.adapter.inbound.message.WorkflowTimerProcessingPortAdapter;
import com.grab.app.workflow.internal.command.handler.StartWorkflowRuntimeCommandHandler;
import com.grab.app.workflow.internal.command.handler.AcceptWorkflowSignalCommandHandler;
import com.grab.app.workflow.internal.config.WorkflowConfig;
import com.grab.app.workflow.internal.port.inbound.WorkflowTimerProcessingPort;
import com.grab.app.workflow.internal.service.StartWorkflowService;
import com.grab.app.workflow.internal.service.WorkflowRuntimeSignalService;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.ChoiceNode;
import com.grab.framework.workflow.TaskNode;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowFailureClassification;
import com.grab.app.workflow.internal.error.WorkflowServiceException;
import com.grab.app.workflow.internal.error.WorkflowServiceError;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowTimerClaim;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.grab.framework.workflow.WorkflowDsl.delay;
import static com.grab.framework.workflow.WorkflowDsl.parallel;
import static com.grab.framework.workflow.WorkflowDsl.task;
import static com.grab.framework.workflow.WorkflowDsl.workflow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(WorkflowRuntimePostgresTest.Config.class)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WorkflowRuntimePostgresTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry properties) {
        properties.add("workflows.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("workflows.datasource.username", POSTGRES::getUsername);
        properties.add("workflows.datasource.password", POSTGRES::getPassword);
        properties.add("workflow.starts-enabled", () -> true);
    }

    @Autowired
    private WorkflowDefinitionRegistry definitions;
    @Autowired
    private WorkflowRuntimeCommandPort commands;
    @Autowired
    private WorkflowRuntimeSignalPort signals;
    @Autowired
    private WorkflowTimerProcessingPort timers;
    @Autowired
    private MutableClock clock;
    @Autowired
    @Qualifier("workflowsDataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("workflowsJobScheduler")
    private ModuleJobScheduler outboxScheduler;

    private JdbcTemplate database;

    @BeforeEach
    void clearSnapshots() {
        outboxScheduler.stop();
        database = new JdbcTemplate(dataSource);
        database.execute("truncate workflow_execution, workflow_outbox_events restart identity cascade");
        clock.time.set(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void start_selectorIncident_persistsWithoutDispatch() {
        TaskNode<String, String> task = task("line", String.class, String.class, "inventory.reserve");
        ChoiceNode<String> choice = new ChoiceNode<>("route", String.class, Map.of("line", task),
                null, input -> "invalid");
        WorkflowDefinition<String, String> definition = workflow("incident", 1, String.class, String.class)
                .root(choice).build();
        definitions.register(definition);
        WorkflowRuntimeCommandPort.StartMessage message = start("incident", "one", "\"sku\"");

        WorkflowRuntimeCommandPort.StartResult result = commands.start(message);

        assertEquals("NEEDS_ATTENTION", result.status());
        assertEquals(result.executionId(), commands.start(message).executionId());
        assertEquals(1L, count("select count(*) from workflow_history"));
        assertEquals(0L, count("select count(*) from workflow_outbox_events"));
    }

    @Test
    void start_concurrentIdenticalAndConflictingRequests_keepsOneReceiptPerKey() throws Exception {
        registerTask("concurrent", "line");
        WorkflowRuntimeCommandPort.StartMessage same = start("concurrent", "same", "\"sku\"");
        CyclicBarrier barrier = new CyclicBarrier(2);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Callable<Id> request = () -> {
                barrier.await(10, TimeUnit.SECONDS);
                return commands.start(same).executionId();
            };
            Future<Id> first = pool.submit(request);
            Future<Id> second = pool.submit(request);
            assertEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));

            CyclicBarrier conflictingBarrier = new CyclicBarrier(2);
            Future<Boolean> left = pool.submit(() -> startCompeting(conflictingBarrier, "\"left\""));
            Future<Boolean> right = pool.submit(() -> startCompeting(conflictingBarrier, "\"right\""));
            int successes = (left.get(10, TimeUnit.SECONDS) ? 1 : 0) + (right.get(10, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successes);
        }
        assertEquals(2L, count("select count(*) from workflow_start_receipt"));
        assertEquals(2L, count("select count(*) from workflow_execution"));
        assertEquals(2L, count("select count(*) from workflow_outbox_events"));
    }

    @Test
    void accept_equivalentOutcomeWithThreeMessageIds_matchesWithoutOverwriting() {
        registerTask("receipts", "line");
        Id id = commands.start(start("receipts", "one", "\"sku\"")).executionId();
        WorkflowRuntimeSignalPort.SignalMessage first = succeeded(id, "one", clock.instant(), "\"reserved\"");
        WorkflowRuntimeSignalPort.SignalMessage second = succeeded(id, "two", clock.instant().plusSeconds(1), "\"reserved\"");
        WorkflowRuntimeSignalPort.SignalMessage third = succeeded(id, "three", clock.instant().plusSeconds(2), "\"reserved\"");

        assertFalse(signals.accept(first).duplicate());
        assertTrue(signals.accept(second).duplicate());
        assertTrue(signals.accept(third).duplicate());
        assertTrue(signals.accept(third).duplicate());
        WorkflowRuntimeSignalPort.SignalMessage changed = succeeded(id, "three", third.occurredAt(), "\"changed\"");
        WorkflowServiceException conflict = assertThrows(WorkflowServiceException.class, () -> signals.accept(changed));
        assertTrue(conflict.getMessageSource() instanceof WorkflowServiceError.Conflict);
        assertEquals(3L, count("select count(*) from workflow_signal_receipt"));
        assertEquals(2L, count("select count(*) from workflow_history"));
    }

    @Test
    void process_expiredClaimCannotFireAfterReclamation() throws Exception {
        WorkflowDefinition<String, String> definition = workflow("lease", 1, String.class, String.class)
                .root(delay("wait", String.class, Duration.ofSeconds(1))).build();
        definitions.register(definition);
        commands.start(start("lease", "one", "\"sku\""));
        clock.time.set(clock.instant().plusSeconds(2));
        assertTrue(AopUtils.isCglibProxy(timers));
        WorkflowTimerClaim oldClaim;
        CyclicBarrier barrier = new CyclicBarrier(2);
        try (ExecutorService workers = Executors.newFixedThreadPool(2)) {
            Callable<Optional<WorkflowTimerClaim>> claim = () -> {
                barrier.await(10, TimeUnit.SECONDS);
                return timers.claim();
            };
            Future<Optional<WorkflowTimerClaim>> first = workers.submit(claim);
            Future<Optional<WorkflowTimerClaim>> second = workers.submit(claim);
            Optional<WorkflowTimerClaim> left = first.get(10, TimeUnit.SECONDS);
            Optional<WorkflowTimerClaim> right = second.get(10, TimeUnit.SECONDS);
            assertTrue(left.isPresent() != right.isPresent());
            oldClaim = left.or(() -> right).orElseThrow();
        }

        clock.time.set(oldClaim.leaseExpiresAt().plusSeconds(1));
        WorkflowTimerClaim current = timers.claim().orElseThrow();
        timers.process(oldClaim);
        assertEquals(0L, count("select count(*) from workflow_timer where status = 'FIRED'"));
        timers.process(current);
        assertEquals(1L, count("select count(*) from workflow_timer where status = 'FIRED'"));
        assertEquals(1L, count("select count(*) from workflow_execution where status = 'COMPLETED'"));
    }

    @Test
    void process_missingPinnedDefinition_recordsQueryableIncident() {
        WorkflowDefinition<String, String> definition = workflow("missing-version", 1, String.class, String.class)
                .root(delay("wait", String.class, Duration.ofSeconds(1))).build();
        definitions.register(definition);
        commands.start(start("missing-version", "one", "\"sku\""));
        database.update("update workflow_execution set definition_version = 2");
        clock.time.set(clock.instant().plusSeconds(2));
        WorkflowTimerClaim claim = timers.claim().orElseThrow();

        timers.process(claim);

        assertEquals(1L, count("select count(*) from workflow_execution where status = 'NEEDS_ATTENTION'"));
        assertEquals(1L, count("select count(*) from workflow_history where event_type = 'RECOVERY_REQUIRED'"));
    }

    @Test
    void start_timerInsertFails_rollsBackExecutionHistoryAndOutbox() {
        registerTask("atomicity", "reject");
        database.execute("alter table workflow_timer add constraint reject_test_timer check (node_id <> 'reject')");
        try {
            WorkflowRuntimeCommandPort.StartMessage message = start("atomicity", "one", "\"sku\"");
            assertThrows(RuntimeException.class, () -> commands.start(message));
            assertEquals(0L, count("select count(*) from workflow_execution"));
            assertEquals(0L, count("select count(*) from workflow_history"));
            assertEquals(0L, count("select count(*) from workflow_outbox_events"));
        } finally {
            database.execute("alter table workflow_timer drop constraint reject_test_timer");
        }
        commands.start(start("atomicity", "one", "\"sku\""));
        assertEquals(1L, count("select count(*) from workflow_timer"));
    }

    @Test
    void accept_concurrentBranchesAndDuplicateSignals_preservesBothOutputs() throws Exception {
        TaskNode<String, String> leftTask = task("left", String.class, String.class, "reserve");
        TaskNode<String, String> rightTask = task("right", String.class, String.class, "price");
        WorkflowDefinition<String, List> definition = workflow("parallel", 1, String.class, List.class)
                .root(parallel("join", leftTask, rightTask)).build();
        definitions.register(definition);
        Id id = commands.start(start("parallel", "one", "\"sku\"")).executionId();
        WorkflowRuntimeSignalPort.SignalMessage left = succeeded(id, "left-message", clock.instant(), "\"a\"", "left");
        WorkflowRuntimeSignalPort.SignalMessage right = succeeded(id, "right-message", clock.instant(), "\"b\"", "right");
        CyclicBarrier barrier = new CyclicBarrier(3);
        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            Future<Boolean> first = pool.submit(() -> acceptCompeting(barrier, left));
            Future<Boolean> duplicate = pool.submit(() -> acceptCompeting(barrier, left));
            Future<Boolean> other = pool.submit(() -> acceptCompeting(barrier, right));
            assertFalse(other.get(10, TimeUnit.SECONDS));
            boolean firstDuplicate = first.get(10, TimeUnit.SECONDS);
            boolean secondDuplicate = duplicate.get(10, TimeUnit.SECONDS);
            assertTrue(firstDuplicate != secondDuplicate);
        }
        String result = database.queryForObject("select result_json from workflow_execution", String.class);
        assertEquals("[\"a\",\"b\"]", result);
        assertEquals(2L, count("select count(*) from workflow_signal_receipt"));
    }

    @Test
    void process_failedHistoryWrite_leavesLeaseReclaimable() {
        WorkflowDefinition<String, String> definition = workflow("rollback-timer", 1, String.class, String.class)
                .root(delay("wait", String.class, Duration.ofSeconds(1))).build();
        definitions.register(definition);
        commands.start(start("rollback-timer", "one", "\"sku\""));
        clock.time.set(clock.instant().plusSeconds(2));
        WorkflowTimerClaim claim = timers.claim().orElseThrow();
        database.execute("alter table workflow_history add constraint reject_test_transition check (event_type <> 'TIMER_DELAY')");
        try {
            assertThrows(RuntimeException.class, () -> timers.process(claim));
            assertEquals(1L, count("select count(*) from workflow_timer where status = 'CLAIMED'"));
            assertEquals(0L, count("select count(*) from workflow_execution where status = 'COMPLETED'"));
        } finally {
            database.execute("alter table workflow_history drop constraint reject_test_transition");
        }
        clock.time.set(claim.leaseExpiresAt().plusSeconds(1));
        WorkflowTimerClaim reclaimed = timers.claim().orElseThrow();
        timers.process(reclaimed);
        assertEquals(1L, count("select count(*) from workflow_timer where status = 'FIRED'"));
        assertEquals(1L, count("select count(*) from workflow_execution where status = 'COMPLETED'"));
    }

    private boolean acceptCompeting(CyclicBarrier barrier, WorkflowRuntimeSignalPort.SignalMessage message) throws Exception {
        barrier.await(10, TimeUnit.SECONDS);
        return signals.accept(message).duplicate();
    }

    private boolean startCompeting(CyclicBarrier barrier, String input) throws Exception {
        barrier.await(10, TimeUnit.SECONDS);
        try {
            commands.start(start("concurrent", "conflict", input));
            return true;
        } catch (WorkflowServiceException exception) {
            if (exception.getMessageSource() instanceof WorkflowServiceError.Conflict) {
                return false;
            }
            throw exception;
        }
    }

    private void registerTask(String name, String nodeId) {
        TaskNode<String, String> task = task(nodeId, String.class, String.class, "inventory.reserve");
        WorkflowDefinition<String, String> definition = workflow(name, 1, String.class, String.class)
                .root(task).build();
        definitions.register(definition);
    }

    private WorkflowRuntimeCommandPort.StartMessage start(String name, String key, String input) {
        return new WorkflowRuntimeCommandPort.StartMessage(name, 1, 1, "scope", input, key);
    }

    private WorkflowRuntimeSignalPort.SignalMessage succeeded(Id id, String messageId, Instant occurredAt, String payload) {
        return succeeded(id, messageId, occurredAt, payload, "line");
    }

    private WorkflowRuntimeSignalPort.SignalMessage succeeded(Id id, String messageId, Instant occurredAt,
                                                             String payload, String nodeId) {
        String operation = id.getValue() + ":forward:" + nodeId;
        return new WorkflowRuntimeSignalPort.SignalMessage(messageId, id, nodeId, operation,
                operation + ":attempt-1", 1, WorkflowPhase.FORWARD, WorkflowRuntimeSignalPort.SignalOutcome.SUCCEEDED,
                payload, 1, "scope", WorkflowFailureClassification.UNCLASSIFIED, null, null, occurredAt);
    }

    private long count(String sql) {
        return database.queryForObject(sql, Long.class);
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan("com.grab.app.workflow.internal.adapter.inbound.message.mapper")
    @EnableConfigurationProperties
    @EnableRetry(proxyTargetClass = true, order = Ordered.HIGHEST_PRECEDENCE)
    @Import({WorkflowConfig.class, StartWorkflowService.class, WorkflowRuntimeSignalService.class,
            WorkflowRuntimeCommandPortAdapter.class, WorkflowRuntimeSignalPortAdapter.class,
            WorkflowTimerProcessingPortAdapter.class, StartWorkflowRuntimeCommandHandler.class,
            AcceptWorkflowSignalCommandHandler.class})
    static class Config {

        @Bean
        @Primary
        @Qualifier("workflowClock")
        MutableClock testClock() {
            return new MutableClock();
        }

        @Bean
        IdGenerator idGenerator() {
            return new IdGenerator() {
                @Override
                public Id generateId() {
                    UUID id = UUID.randomUUID();
                    return new CommonId(id.toString());
                }

                @Override
                public Id convertIdFrom(String id) {
                    return new CommonId(id);
                }
            };
        }
    }

    static final class MutableClock extends Clock {

        private final AtomicReference<Instant> time = new AtomicReference<>();

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            Clock replacement = Clock.fixed(instant(), zone);
            return replacement;
        }

        @Override
        public Instant instant() {
            return time.get();
        }
    }
}
