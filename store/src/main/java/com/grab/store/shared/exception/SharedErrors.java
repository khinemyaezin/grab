package com.grab.store.shared.exception;

import java.util.List;
import java.util.Map;

public final class SharedErrors {

    private SharedErrors() {
    }

    public static SharedException requestValidation(List<Map<String, Object>> errors) {
        return new SharedException(
                new SharedError.RequestValidation(errors),
                "Request validation failed."
        );
    }

    public static SharedException malformedJson(String reason) {
        return new SharedException(
                new SharedError.RequestMalformedJson(reason == null ? "unknown" : reason),
                "Malformed JSON request body."
        );
    }

    public static SharedException internalUnexpected() {
        return new SharedException(
                new SharedError.InternalUnexpected(),
                "Unexpected internal error."
        );
    }

    public static SharedException workflowScopeForbidden(String platformCode, String scopeKey, String scopeId) {
        return new SharedException(
                new SharedError.WorkflowScopeForbidden(platformCode, scopeKey, scopeId),
                "A Seller Portal merchant account scope is required for workflows."
        );
    }

    public static SharedException workflowNotFound(String workflowId) {
        return new SharedException(
                new SharedError.WorkflowNotFound(workflowId),
                "Workflow not found."
        );
    }
}
