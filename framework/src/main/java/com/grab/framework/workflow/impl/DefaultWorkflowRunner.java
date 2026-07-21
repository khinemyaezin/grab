package com.grab.framework.workflow.impl;

import com.grab.framework.workflow.WorkflowContext;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowResult;
import com.grab.framework.workflow.WorkflowRunner;
import com.grab.framework.workflow.WorkflowStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DefaultWorkflowRunner implements WorkflowRunner {

    @Override
    public WorkflowResult run(WorkflowDefinition definition, WorkflowContext context) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(context, "context");

        List<CompletedStep> completedSteps = new ArrayList<>();

        for (WorkflowStep step : definition.steps()) {
            try {
                Object output = step.execute(context);
                completedSteps.add(new CompletedStep(step, output));
                context.put(step.name(), output);
            } catch (RuntimeException exception) {
                boolean compensationCompleted = compensateCompletedSteps(completedSteps, context);
                return WorkflowResult.failed(
                        definition.name(),
                        context,
                        step.name(),
                        exception,
                        compensationCompleted
                );
            }
        }

        return WorkflowResult.success(definition.name(), context);
    }

    private boolean compensateCompletedSteps(List<CompletedStep> completedSteps, WorkflowContext context) {
        boolean compensationCompleted = true;
        for (int index = completedSteps.size() - 1; index >= 0; index--) {
            CompletedStep completedStep = completedSteps.get(index);
            try {
                completedStep.step().compensate(context, completedStep.output());
            } catch (RuntimeException compensationException) {
                compensationCompleted = false;
            }
        }
        return compensationCompleted;
    }

    private record CompletedStep(WorkflowStep step, Object output) {
    }
}
