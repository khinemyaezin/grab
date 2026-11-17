package com.grab.framework.workflow;

import java.util.List;
import java.util.Set;

public sealed interface WorkflowNode permits TaskNode, SequenceNode, ChoiceNode, ParallelNode, WaitForSignalNode, DelayNode, PassThroughNode, LocalMapNode, BoundTaskNode, JoinedParallelNode {

    String id();

    WorkflowNodeKind kind();

    Class<?> inputType();

    Class<?> outputType();

    List<WorkflowNode> children();

    Set<String> referencedNodeIds();
}
