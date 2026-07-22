package com.grab.framework.workflow;

import java.util.Objects;

public sealed interface WorkflowResult permits WorkflowResult.Success, WorkflowResult.Failed {

    String workflowName();

    String workflowId();

    WorkflowContext context();

    boolean succeeded();

    record Success(
            String workflowName,
            String workflowId,
            WorkflowContext context
    ) implements WorkflowResult {
        public Success {
            Objects.requireNonNull(workflowName, "workflowName");
            Objects.requireNonNull(workflowId, "workflowId");
            Objects.requireNonNull(context, "context");
        }

        @Override
        public boolean succeeded() {
            return true;
        }
    }

    record Failed(
            String workflowName,
            String workflowId,
            WorkflowContext context,
            String failedStep,
            Throwable cause,
            boolean compensationCompleted
    ) implements WorkflowResult {
        public Failed {
            Objects.requireNonNull(workflowName, "workflowName");
            Objects.requireNonNull(workflowId, "workflowId");
            Objects.requireNonNull(context, "context");
            Objects.requireNonNull(failedStep, "failedStep");
            Objects.requireNonNull(cause, "cause");
        }

        @Override
        public boolean succeeded() {
            return false;
        }
    }

    static Success success(String workflowName, String workflowId, WorkflowContext context) {
        return new Success(workflowName, workflowId, context);
    }

    static Failed failed(
            String workflowName,
            String workflowId,
            WorkflowContext context,
            String failedStep,
            Throwable cause,
            boolean compensationCompleted
    ) {
        return new Failed(workflowName, workflowId, context, failedStep, cause, compensationCompleted);
    }
}
