package com.grab.framework.workflow;

import java.util.Objects;

public record CompensationInput<I, O>(I input, O output) {

    public CompensationInput {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
    }
}
