package com.saleschannel.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.List;
import java.util.Objects;

public record ApplySalesChannelRegionBindingsCommand(List<Binding> bindings) implements Command<Integer> {
    public ApplySalesChannelRegionBindingsCommand {
        bindings = List.copyOf(bindings);
        if (bindings.isEmpty()) {
            throw new IllegalArgumentException("at least one channel region binding is required");
        }
    }

    public record Binding(
            Id salesChannelId,
            Id regionId,
            boolean enabled,
            boolean defaultRegion
    ) {
        public Binding {
            Objects.requireNonNull(salesChannelId, "salesChannelId is required");
            Objects.requireNonNull(regionId, "regionId is required");
            if (defaultRegion && !enabled) {
                throw new IllegalArgumentException("a disabled binding cannot be default");
            }
        }
    }
}
