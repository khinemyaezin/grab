package com.grab.framework.workflow;

import java.util.Objects;

public sealed interface WorkflowResult permits WorkflowResult.Success, WorkflowResult.Failed {

    String workflowName();

    WorkflowContext context();

    boolean succeeded();

    record Success(String workflowName, WorkflowContext context) implements WorkflowResult {
        public Success {
            Objects.requireNonNull(workflowName, "workflowName");
            Objects.requireNonNull(context, "context");
        }

        @Override
        public boolean succeeded() {
            return true;
        }
    }

    record Failed(
            String workflowName,
            WorkflowContext context,
            String failedStep,
            Throwable cause,
            boolean compensationCompleted
    ) implements WorkflowResult {
        public Failed {
            Objects.requireNonNull(workflowName, "workflowName");
            Objects.requireNonNull(context, "context");
            Objects.requireNonNull(failedStep, "failedStep");
            Objects.requireNonNull(cause, "cause");
        }

        @Override
        public boolean succeeded() {
            return false;
        }
    }

    static Success success(String workflowName, WorkflowContext context) {
        return new Success(workflowName, context);
    }

    static Failed failed(
            String workflowName,
            WorkflowContext context,
            String failedStep,
            Throwable cause,
            boolean compensationCompleted
    ) {
        return new Failed(workflowName, context, failedStep, cause, compensationCompleted);
    }
}
