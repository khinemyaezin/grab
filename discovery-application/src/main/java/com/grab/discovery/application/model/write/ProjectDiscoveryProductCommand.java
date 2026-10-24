package com.grab.discovery.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.Instant;

public record ProjectDiscoveryProductCommand(ProjectionUpdate update) implements Command<Void> {
    public sealed interface ProjectionUpdate permits RefreshVariant, RefreshProduct, RefreshProductChannel, RefreshSku, RefreshLocation, RefreshChannel {
        Instant occurredAt();
    }

    public record RefreshVariant(Id variantId, Id salesChannelId, Instant occurredAt) implements ProjectionUpdate {
    }

    public record RefreshProduct(Id productId, Instant occurredAt) implements ProjectionUpdate {
    }

    public record RefreshProductChannel(Id productId, Id salesChannelId, Instant occurredAt) implements ProjectionUpdate {
    }

    public record RefreshSku(String sku, Instant occurredAt) implements ProjectionUpdate {
    }

    public record RefreshLocation(Id locationId, Id salesChannelId, Instant occurredAt) implements ProjectionUpdate {
    }

    public record RefreshChannel(Id salesChannelId, Instant occurredAt) implements ProjectionUpdate {
    }
}
