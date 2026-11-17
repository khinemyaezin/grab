package com.grab.framework.workflow;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Map;

public final class WorkflowDsl {

    private WorkflowDsl() {
    }

    public static <I> WorkflowFlow<I, I> flow(Class<I> input) {
        return new WorkflowFlow<>(input, input, List.of());
    }

    public static <Q, R> WorkflowStep<Q, R> operation(String id, WorkflowOperation<Q, R> operation) {
        TaskNode<Q, R> node = operationTask(id, operation, Optional.empty());
        return new WorkflowStep<>(node, operation.requestType(), operation.resultType());
    }

    public static <S, Q, R, T> WorkflowStep<S, T> bind(
            String id, Class<S> input, Class<T> output, WorkflowOperation<Q, R> operation,
            Function<? super S, ? extends Q> request, BiFunction<? super S, ? super R, ? extends T> result) {
        return bind(id, input, output, operation, request, result, Optional.empty());
    }

    public static <S, Q, R, T> WorkflowStep<S, T> bind(
            String id, Class<S> input, Class<T> output, WorkflowOperation<Q, R> operation,
            Function<? super S, ? extends Q> request, BiFunction<? super S, ? super R, ? extends T> result,
            Optional<CompensationDefinition> compensation) {
        TaskNode<Q, R> task = operationTask(id + ".task", operation, compensation);
        BoundTaskNode<S, Q, R, T> node = new BoundTaskNode<>(id, input, output, task, request, result);
        return new WorkflowStep<>(node, input, output);
    }

    private static <Q, R> TaskNode<Q, R> operationTask(
            String id, WorkflowOperation<Q, R> operation, Optional<CompensationDefinition> compensation) {
        Duration timeout = Duration.ofMinutes(10);
        return new TaskNode<>(id, operation.requestType(), operation.resultType(), operation.name(),
                timeout, RetryPolicy.DEFAULT, compensation, operation.effect());
    }

    public static <I, O> WorkflowStep<I, O> map(
            String id, Class<I> input, Class<O> output, Function<? super I, ? extends O> mapper) {
        LocalMapNode<I, O> node = new LocalMapNode<>(id, input, output, mapper);
        return new WorkflowStep<>(node, input, output);
    }

    public static <T> WorkflowStep<T, T> when(String id, Predicate<? super T> predicate, WorkflowStep<T, T> run) {
        Class<T> type = run.inputType();
        PassThroughNode<T> skipNode = passThrough(id + ".skip", type);
        WorkflowStep<T, T> skip = new WorkflowStep<>(skipNode, type, type);
        Map<String, WorkflowStep<T, T>> branches = new LinkedHashMap<>();
        branches.put("run", run);
        branches.put("skip", skip);
        return choose(id, type, type, branches, "skip", value -> predicate.test(value) ? "run" : "skip");
    }

    public static <I, O> WorkflowStep<I, O> choose(
            String id, Class<I> input, Class<O> output, Map<String, WorkflowStep<I, O>> branches,
            String fallback, ChoiceSelector<? super I> selector) {
        Map<String, WorkflowNode> nodes = new LinkedHashMap<>();
        branches.forEach((label, step) -> nodes.put(label, step.node()));
        ChoiceNode<I> node = new ChoiceNode<>(id, input, nodes, fallback, selector, false);
        return new WorkflowStep<>(node, input, output);
    }

    public static <I, L, R, O> WorkflowStep<I, O> join(
            String id, WorkflowStep<I, L> left, WorkflowStep<I, R> right, Class<O> output,
            BiFunction<? super L, ? super R, ? extends O> mapper) {
        JoinedParallelNode<I, L, R, O> node = new JoinedParallelNode<>(id, left, right, output, mapper);
        return new WorkflowStep<>(node, left.inputType(), output);
    }

    public static <I, O> WorkflowDefinitionBuilder<I, O> workflow(
            String name,
            int version,
            Class<I> inputType,
            Class<O> outputType
    ) {
        return new WorkflowDefinitionBuilder<>(name, version, inputType, outputType);
    }

    public static <I, O> TaskNode<I, O> task(
            String id,
            Class<I> inputType,
            Class<O> outputType,
            String operation
    ) {
        return new TaskNode<>(id, inputType, outputType, operation);
    }

    public static SequenceNode sequence(String id, WorkflowNode... children) {
        return new SequenceNode(id, List.of(children));
    }

    public static ParallelNode parallel(String id, WorkflowNode... branches) {
        return new ParallelNode(id, List.of(branches));
    }

    public static <I> ChoiceNode<I> choice(
            String id,
            Class<I> inputType,
            Map<String, WorkflowNode> branches,
            String defaultBranch
    ) {
        return new ChoiceNode<>(id, inputType, branches, defaultBranch);
    }

    public static <I> ChoiceNode<I> choice(
            String id,
            Class<I> inputType,
            Map<String, WorkflowNode> branches,
            String defaultBranch,
            ChoiceSelector<? super I> selector
    ) {
        return new ChoiceNode<>(id, inputType, branches, defaultBranch, selector);
    }

    public static WaitForSignalNode waitForSignal(
            String id,
            Class<?> inputType,
            String signalType,
            Class<?> payloadType,
            Duration timeout
    ) {
        return new WaitForSignalNode(id, inputType, signalType, payloadType, timeout);
    }

    public static DelayNode delay(String id, Class<?> inputType, Duration delay) {
        return new DelayNode(id, inputType, delay);
    }

    public static <T> PassThroughNode<T> passThrough(String id, Class<T> type) {
        return new PassThroughNode<>(id, type);
    }

    public static final class WorkflowDefinitionBuilder<I, O> {

        private final String name;
        private final int version;
        private final Class<I> inputType;
        private final Class<O> outputType;
        private WorkflowNode root;
        private String outputNodeId;

        private WorkflowDefinitionBuilder(String name, int version, Class<I> inputType, Class<O> outputType) {
            this.name = name;
            this.version = version;
            this.inputType = inputType;
            this.outputType = outputType;
        }

        public WorkflowDefinitionBuilder<I, O> root(WorkflowNode root) {
            this.root = root;
            return this;
        }

        public WorkflowDefinitionBuilder<I, O> outputNode(String outputNodeId) {
            this.outputNodeId = outputNodeId;
            return this;
        }

        public WorkflowDefinition<I, O> build() {
            if (root == null) {
                throw new IllegalStateException("workflow root is required");
            }
            String resolvedOutputNode = outputNodeId == null ? root.id() : outputNodeId;
            return WorkflowDefinition.of(name, version, inputType, outputType, root, resolvedOutputNode);
        }
    }
}
