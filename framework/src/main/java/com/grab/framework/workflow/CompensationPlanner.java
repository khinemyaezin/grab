package com.grab.framework.workflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompensationPlanner {

    public CompensationPlan plan(WorkflowDefinition<?, ?> definition, Set<String> completedNodeIds) {
        Set<String> completed = Set.copyOf(completedNodeIds);
        Map<String, TaskNode<?, ?>> compensable = new LinkedHashMap<>();
        for (WorkflowNode node : definition.nodes().values()) {
            if (node instanceof TaskNode<?, ?> task && completed.contains(task.id()) && task.compensation().isPresent()) {
                compensable.put(task.id(), task);
            }
        }

        Map<String, Set<String>> blockers = new LinkedHashMap<>();
        for (TaskNode<?, ?> task : compensable.values()) {
            blockers.put(task.id(), new LinkedHashSet<>());
        }
        for (TaskNode<?, ?> task : compensable.values()) {
            for (String dependency : task.compensation().orElseThrow().dependsOn()) {
                if (compensable.containsKey(dependency)) {
                    blockers.get(dependency).add(task.id());
                }
            }
        }

        List<List<CompensationAction>> waves = new ArrayList<>();
        Set<String> remaining = new LinkedHashSet<>(compensable.keySet());
        while (!remaining.isEmpty()) {
            List<CompensationAction> wave = new ArrayList<>();
            for (String nodeId : remaining) {
                Set<String> taskBlockers = blockers.get(nodeId);
                if (remaining.stream().noneMatch(taskBlockers::contains)) {
                    TaskNode<?, ?> task = compensable.get(nodeId);
                    CompensationDefinition compensation = task.compensation().orElseThrow();
                    wave.add(new CompensationAction(
                            nodeId,
                            compensation.operation(),
                            compensation.timeout(),
                            compensation.retryPolicy()
                    ));
                }
            }
            if (wave.isEmpty()) {
                throw new IllegalArgumentException("Compensation dependencies contain a cycle");
            }
            waves.add(List.copyOf(wave));
            wave.forEach(action -> remaining.remove(action.nodeId()));
        }
        return new CompensationPlan(waves);
    }
}
