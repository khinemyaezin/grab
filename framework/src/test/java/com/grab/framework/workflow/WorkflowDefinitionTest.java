package com.grab.framework.workflow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.grab.framework.workflow.WorkflowDsl.choice;
import static com.grab.framework.workflow.WorkflowDsl.parallel;
import static com.grab.framework.workflow.WorkflowDsl.passThrough;
import static com.grab.framework.workflow.WorkflowDsl.sequence;
import static com.grab.framework.workflow.WorkflowDsl.task;
import static com.grab.framework.workflow.WorkflowDsl.waitForSignal;
import static com.grab.framework.workflow.WorkflowDsl.workflow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowDefinitionTest {

    @Test
    void buildsVersionedDefinitionWithParallelAndChoiceNodes() {
        TaskNode<String, String> create = task("create", String.class, String.class, "catalog.create");
        TaskNode<String, Integer> price = task("price", String.class, Integer.class, "pricing.calculate");
        TaskNode<String, Integer> stock = task("stock", String.class, Integer.class, "inventory.reserve");
        WaitForSignalNode confirmed = waitForSignal("confirmed", List.class, "checkout.confirmed", String.class, Duration.ofMinutes(5));
        TaskNode<List, String> retryCreate = task("retry-create", List.class, String.class, "catalog.retry");
        ParallelNode preparation = parallel("prepare", price, stock);
        ChoiceNode<List> route = choice("route", List.class, Map.of("confirmed", confirmed, "retry", retryCreate), null);

        WorkflowDefinition<String, String> definition = workflow("checkout", 2, String.class, String.class)
                .root(sequence("root", create, preparation, route))
                .outputNode("route")
                .build();

        assertEquals("checkout", definition.name());
        assertEquals(2, definition.version());
        assertEquals(8, definition.nodes().size());
        assertEquals(WorkflowNodeKind.PARALLEL, definition.nodes().get("prepare").kind());
    }

    @Test
    void rejectsDuplicateNodeIds() {
        TaskNode<String, String> first = task("same", String.class, String.class, "one");
        TaskNode<String, String> second = task("same", String.class, String.class, "two");

        assertThrows(IllegalArgumentException.class, () -> workflow("duplicate", 1, String.class, String.class)
                .root(sequence("root", first, second))
                .build());
    }

    @Test
    void rejectsUnknownAndCyclicCompensationDependencies() {
        TaskNode<String, String> unknown = task("create", String.class, String.class, "catalog.create")
                .withCompensation(CompensationDefinition.of("catalog.delete", List.of("missing")));
        assertThrows(IllegalArgumentException.class, () -> workflow("unknown", 1, String.class, String.class).root(unknown).build());

        TaskNode<String, String> first = task("first", String.class, String.class, "first")
                .withCompensation(CompensationDefinition.of("undo-first", List.of("second")));
        TaskNode<String, String> second = task("second", String.class, String.class, "second")
                .withCompensation(CompensationDefinition.of("undo-second", List.of("first")));
        assertThrows(IllegalArgumentException.class, () -> workflow("cycle", 1, String.class, String.class)
                .root(parallel("root", first, second))
                .build());
    }

    @Test
    void rejectsMismatchedOutputAndOptionalChoiceOutput() {
        TaskNode<String, Integer> calculate = task("calculate", String.class, Integer.class, "pricing.calculate");
        assertThrows(IllegalArgumentException.class, () -> workflow("output", 1, String.class, String.class).root(calculate).build());

        TaskNode<String, String> confirmed = task("confirmed", String.class, String.class, "confirmed");
        TaskNode<String, String> retry = task("retry", String.class, String.class, "retry");
        ChoiceNode<String> choice = choice("route", String.class, Map.of("confirmed", confirmed, "retry", retry), null);
        assertThrows(IllegalArgumentException.class, () -> workflow("optional", 1, String.class, String.class)
                .root(choice)
                .outputNode("confirmed")
                .build());
    }

    @Test
    void rejectsIncompatibleRootInput() {
        TaskNode<String, String> task = task("task", String.class, String.class, "task");
        assertThrows(IllegalArgumentException.class, () -> workflow("input", 1, Object.class, String.class).root(task).build());
    }

    @Test
    void defaultRetryPolicyUsesBoundedExponentialBackoff() {
        assertEquals(Duration.ofSeconds(1), RetryPolicy.DEFAULT.delayForAttempt(1));
        assertEquals(Duration.ofSeconds(2), RetryPolicy.DEFAULT.delayForAttempt(2));
        assertEquals(Duration.ofSeconds(4), RetryPolicy.DEFAULT.delayForAttempt(3));
        assertEquals(Duration.ofSeconds(30), RetryPolicy.DEFAULT.delayForAttempt(10));
    }

    @Test
    void passThroughNodePreservesItsDeclaredType() {
        PassThroughNode<String> node = passThrough("unchanged", String.class);
        WorkflowDefinition<String, String> definition = workflow("pass-through", 1, String.class, String.class)
                .root(node)
                .build();

        assertEquals(String.class, definition.nodes().get("unchanged").outputType());
        assertEquals(WorkflowNodeKind.PASS_THROUGH, node.kind());
    }

    @Test
    void readOnlyTaskCannotDeclareCompensation() {
        assertThrows(IllegalArgumentException.class, () -> task("assert", String.class, String.class, "catalog.assert")
                .readOnly()
                .withCompensation(CompensationDefinition.of("catalog.undo-assert", List.of())));
    }

    @Test
    void registryRequiresExactDefinitionVersion() {
        TaskNode<String, String> publish = task("publish", String.class, String.class, "catalog.publish");
        WorkflowDefinition<String, String> definition = workflow("catalog", 4, String.class, String.class).root(publish).build();
        WorkflowDefinitionRegistry registry = new WorkflowDefinitionRegistry();
        registry.register(definition);
        assertEquals(definition, registry.require("catalog", 4));
        assertThrows(IllegalArgumentException.class, () -> registry.require("catalog", 3));
        assertThrows(IllegalArgumentException.class, () -> registry.register(definition));
    }

    @Test
    void compensationPlanRunsDependentActionsInWaves() {
        TaskNode<String, String> create = task("create", String.class, String.class, "catalog.create")
                .withCompensation(CompensationDefinition.of("catalog.delete", List.of()));
        TaskNode<String, String> publish = task("publish", String.class, String.class, "catalog.publish")
                .withCompensation(CompensationDefinition.of("catalog.unpublish", List.of("create")));
        WorkflowDefinition<String, String> definition = workflow("compensation", 1, String.class, String.class)
                .root(sequence("root", create, publish))
                .build();

        CompensationPlan plan = new CompensationPlanner().plan(definition, java.util.Set.of("create", "publish"));
        assertEquals(2, plan.waves().size());
        assertEquals("publish", plan.waves().getFirst().getFirst().nodeId());
        assertEquals("create", plan.waves().getLast().getFirst().nodeId());
    }

}
