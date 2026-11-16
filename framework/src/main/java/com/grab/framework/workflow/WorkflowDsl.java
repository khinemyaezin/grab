package com.grab.framework.workflow;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class WorkflowDsl {

    private WorkflowDsl() {
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
