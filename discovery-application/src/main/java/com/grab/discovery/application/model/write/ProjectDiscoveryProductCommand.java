package com.grab.discovery.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.Instant;

public record ProjectDiscoveryProductCommand(
        ProjectionUpdate update
) implements Command<Void> {

    public sealed interface ProjectionUpdate permits
            Published,
            Unpublished,
            CatalogVariantChanged,
            ProductStatusChanged,
            StockChanged,
            RouteChanged,
            PriceSetChanged,
            ChannelEnabled {
    }

    public record Published(
            Id variantId,
            Id salesChannelId,
            Instant occurredAt
    ) implements ProjectionUpdate {
    }

    public record Unpublished(
            Id variantId,
            Id salesChannelId,
            Instant occurredAt
    ) implements ProjectionUpdate {
    }

    public record CatalogVariantChanged(
            Id variantId,
            Instant occurredAt
    ) implements ProjectionUpdate {
    }

    public record ProductStatusChanged(
            Id productId,
            String newStatus,
            Instant occurredAt
    ) implements ProjectionUpdate {
    }

    public record StockChanged(
            String sku,
            Instant occurredAt
    ) implements ProjectionUpdate {
    }

    public record RouteChanged(
            Id locationId,
            Id salesChannelId,
            Instant occurredAt
    ) implements ProjectionUpdate {
    }

    public record PriceSetChanged(
            Id priceSetId,
            Instant occurredAt
    ) implements ProjectionUpdate {
    }

    public record ChannelEnabled(
            Id salesChannelId,
            boolean enabled,
            Instant occurredAt
    ) implements ProjectionUpdate {
    }
}
