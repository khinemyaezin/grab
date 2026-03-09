package com.grab.framework.exception;

import java.util.Map;

public sealed interface FrameworkError extends MessageSource permits FrameworkError.CqrsHandlerMissing {

    record CqrsHandlerMissing(String type, String requestClass) implements FrameworkError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "shr.internal.cqrs_handler_missing";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "type", type,
                    "requestClass", requestClass
            );
        }
    }
}
