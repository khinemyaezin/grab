package com.grab.framework.workflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class WorkflowDefinition<I, O> {

    private final String name;
    private final int version;
    private final Class<I> inputType;
    private final Class<O> outputType;
    private final WorkflowNode root;
    private final String outputNodeId;
    private final Map<String, WorkflowNode> nodes;

    private WorkflowDefinition(
            String name,
            int version,
            Class<I> inputType,
            Class<O> outputType,
            WorkflowNode root,
            String outputNodeId
    ) {
        this.name = requireText(name, "name");
        if (version < 1) {
            throw new IllegalArgumentException("version must be positive");
        }
        this.version = version;
        this.inputType = Objects.requireNonNull(inputType, "inputType");
        this.outputType = Objects.requireNonNull(outputType, "outputType");
        this.root = Objects.requireNonNull(root, "root");
        this.outputNodeId = requireText(outputNodeId, "outputNodeId");

        Map<String, WorkflowNode> discovered = new LinkedHashMap<>();
        collect(root, discovered);
        if (!discovered.containsKey(outputNodeId)) {
            throw new IllegalArgumentException("outputNodeId must reference a node in the workflow");
        }
        WorkflowNode outputNode = discovered.get(outputNodeId);
        if (!outputType.isAssignableFrom(outputNode.outputType())) {
            throw new IllegalArgumentException(
                    "output node type " + outputNode.outputType().getName()
                            + " is not assignable to " + outputType.getName()
            );
        }
        if (!root.inputType().isAssignableFrom(inputType)) {
            throw new IllegalArgumentException(
                    "root input type " + root.inputType().getName()
                            + " is not assignable to " + inputType.getName()
            );
        }
        if (!containsOnEverySuccessfulPath(root, outputNodeId)) {
            throw new IllegalArgumentException("output node must execute on every successful path: " + outputNodeId);
        }
        for (WorkflowNode node : discovered.values()) {
            for (String reference : node.referencedNodeIds()) {
                if (!discovered.containsKey(reference)) {
                    throw new IllegalArgumentException(
                            "node '" + node.id() + "' references unknown node '" + reference + "'"
                    );
                }
                if (node instanceof TaskNode<?, ?> task
                        && task.compensation().isPresent()
                        && (!(discovered.get(reference) instanceof TaskNode<?, ?> dependencyTask
                        && dependencyTask.compensation().isPresent()) || task.id().equals(reference))) {
                    throw new IllegalArgumentException(
                            "compensation dependency must reference a compensable task: " + reference
                    );
                }
            }
        }
        validateCompensationCycles(discovered);
        this.nodes = Collections.unmodifiableMap(new LinkedHashMap<>(discovered));
    }

    public static <I, O> WorkflowDefinition<I, O> of(
            String name,
            int version,
            Class<I> inputType,
            Class<O> outputType,
            WorkflowNode root,
            String outputNodeId
    ) {
        return new WorkflowDefinition<>(name, version, inputType, outputType, root, outputNodeId);
    }

    public String name() {
        return name;
    }

    public int version() {
        return version;
    }

    public Class<I> inputType() {
        return inputType;
    }

    public Class<O> outputType() {
        return outputType;
    }

    public WorkflowNode root() {
        return root;
    }

    public String outputNodeId() {
        return outputNodeId;
    }

    public Map<String, WorkflowNode> nodes() {
        return nodes;
    }

    private static void collect(WorkflowNode node, Map<String, WorkflowNode> discovered) {
        if (node.id().startsWith("comp:")) {
            throw new IllegalArgumentException("Node ID uses the reserved compensation prefix: " + node.id());
        }
        WorkflowNode previous = discovered.put(node.id(), node);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate node ID: " + node.id());
        }
        node.children().forEach(child -> collect(child, discovered));
    }

    private static boolean containsOnEverySuccessfulPath(WorkflowNode node, String targetId) {
        if (node.id().equals(targetId)) {
            return true;
        }
        if (node instanceof SequenceNode sequence) {
            return sequence.children().stream().anyMatch(child -> containsOnEverySuccessfulPath(child, targetId));
        }
        if (node instanceof ChoiceNode<?> choice) {
            return choice.branches().values().stream()
                    .allMatch(branch -> containsOnEverySuccessfulPath(branch, targetId));
        }
        if (node instanceof ParallelNode parallel) {
            return parallel.branches().stream()
                    .anyMatch(branch -> containsOnEverySuccessfulPath(branch, targetId));
        }
        return false;
    }

    private static void validateCompensationCycles(Map<String, WorkflowNode> nodes) {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (WorkflowNode node : nodes.values()) {
            if (node instanceof TaskNode<?, ?> task && task.compensation().isPresent()) {
                graph.put(task.id(), Set.copyOf(task.compensation().orElseThrow().dependsOn()));
            }
        }
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String nodeId : graph.keySet()) {
            visitCompensation(nodeId, graph, visiting, visited);
        }
    }

    private static void visitCompensation(
            String nodeId,
            Map<String, Set<String>> graph,
            Set<String> visiting,
            Set<String> visited
    ) {
        if (visited.contains(nodeId)) {
            return;
        }
        if (!visiting.add(nodeId)) {
            throw new IllegalArgumentException("Compensation dependencies contain a cycle");
        }
        for (String dependency : graph.getOrDefault(nodeId, Set.of())) {
            if (graph.containsKey(dependency)) {
                visitCompensation(dependency, graph, visiting, visited);
            }
        }
        visiting.remove(nodeId);
        visited.add(nodeId);
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
