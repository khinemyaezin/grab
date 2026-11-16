package com.grab.framework.workflow;

import java.util.List;
import java.util.Objects;

public record CompensationPlan(List<List<CompensationAction>> waves) {

    public CompensationPlan {
        Objects.requireNonNull(waves, "waves");
        waves = waves.stream().map(List::copyOf).toList();
    }

    public boolean isEmpty() {
        return waves.isEmpty();
    }
}
