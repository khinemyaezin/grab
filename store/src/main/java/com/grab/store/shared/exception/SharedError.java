package com.grab.store.shared.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.List;
import java.util.Map;

public sealed interface SharedError extends MessageSource permits
        SharedError.RequestValidation,
        SharedError.RequestMalformedJson,
        SharedError.InternalUnexpected,
        SharedError.WorkflowScopeForbidden,
        SharedError.WorkflowNotFound {

    record RequestValidation(List<Map<String, Object>> errors) implements SharedError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "shr.request.validation";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("errors", errors);
        }
    }

    record RequestMalformedJson(String reason) implements SharedError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "shr.request.malformed_json";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reason", reason);
        }
    }

    record InternalUnexpected() implements SharedError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "shr.internal.unexpected";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record WorkflowScopeForbidden(String platformCode, String scopeKey, String scopeId) implements SharedError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "shr.workflow.scope_forbidden";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "platformCode", platformCode,
                    "scopeKey", scopeKey,
                    "scopeId", scopeId
            );
        }
    }

    record WorkflowNotFound(String workflowId) implements SharedError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "shr.workflow.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("workflowId", workflowId);
        }
    }
}
