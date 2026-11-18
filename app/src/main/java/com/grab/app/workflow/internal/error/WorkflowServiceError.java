package com.grab.app.workflow.internal.error;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface WorkflowServiceError extends MessageSource {

    @Override
    default Map<String, Object> args() {
        return Map.of();
    }

    record Conflict() implements WorkflowServiceError {
        @Override public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override public String code() { return "workflow.service.request.conflict"; }
    }

    record InvalidCorrelation() implements WorkflowServiceError {
        @Override public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        @Override public String code() { return "workflow.service.signal.invalid_correlation"; }
    }

    record RecoveryRequired() implements WorkflowServiceError {
        @Override public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override public String code() { return "workflow.service.execution.recovery_required"; }
    }

    record ConcurrentUpdate() implements WorkflowServiceError {
        @Override public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        @Override public String code() { return "workflow.service.execution.concurrent_update"; }
    }
}
