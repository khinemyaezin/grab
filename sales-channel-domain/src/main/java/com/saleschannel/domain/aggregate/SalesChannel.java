package com.saleschannel.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.domain.event.SalesChannelCreatedEvent;
import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;
import com.saleschannel.domain.exception.SalesChannelDomainError;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import com.saleschannel.domain.valueobject.ChannelRegion;
import lombok.Getter;
import lombok.AccessLevel;

import java.time.Instant;
import java.util.Objects;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
public class SalesChannel extends AggregateRoot<Id> {
    private final String name;
    private final ChannelType type;
    private final ChannelOwner owner;
    private final Id merchantId;
    private ChannelStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;
    @Getter(AccessLevel.NONE)
    private final Set<ChannelRegion> supportedRegions;

    public SalesChannel(
            Id id,
            String name,
            ChannelType type,
            ChannelOwner owner,
            Id merchantId,
            ChannelStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version,
            Set<ChannelRegion> supportedRegions
    ) {
        super(id);
        this.name = requireName(name);
        this.type = Objects.requireNonNull(type, "type is required");
        this.owner = Objects.requireNonNull(owner, "owner is required");
        this.merchantId = merchantId;
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.version = version;
        this.supportedRegions = new HashSet<>(supportedRegions == null ? Set.of() : supportedRegions);
        validateSupportedRegions();
        validateOwnership();
    }

    public Set<ChannelRegion> getSupportedRegions() {
        return Set.copyOf(supportedRegions);
    }

    public void enableRegion(Id regionId, boolean isDefault) {
        Objects.requireNonNull(regionId, "regionId is required");
        if (isDefault) {
            Set<ChannelRegion> updated = new HashSet<>();
            for (ChannelRegion binding : supportedRegions) {
                updated.add(new ChannelRegion(binding.salesChannelId(), binding.regionId(), binding.enabled(), false));
            }
            supportedRegions.clear();
            supportedRegions.addAll(updated);
        }
        supportedRegions.removeIf(binding -> binding.regionId().equals(regionId));
        supportedRegions.add(new ChannelRegion(getId(), regionId, true, isDefault));
    }

    public void disableRegion(Id regionId) {
        Objects.requireNonNull(regionId, "regionId is required");
        Optional<ChannelRegion> binding = supportedRegions.stream()
                .filter(candidate -> candidate.regionId().equals(regionId))
                .findFirst();
        binding.ifPresent(existing -> {
            supportedRegions.remove(existing);
            supportedRegions.add(new ChannelRegion(getId(), regionId, false, false));
        });
    }

    public boolean servesRegion(Id regionId) {
        return supportedRegions.stream()
                .anyMatch(binding -> binding.regionId().equals(regionId) && binding.enabled());
    }

    public Optional<ChannelRegion> getDefaultRegion() {
        return supportedRegions.stream()
                .filter(ChannelRegion::enabled)
                .filter(ChannelRegion::isDefault)
                .findFirst();
    }

    private void validateSupportedRegions() {
        long defaults = supportedRegions.stream().filter(ChannelRegion::isDefault).count();
        if (defaults > 1) {
            throw new IllegalArgumentException("a sales channel can have only one default region");
        }
        if (supportedRegions.stream().anyMatch(binding -> !binding.salesChannelId().equals(getId()))) {
            throw new IllegalArgumentException("region binding belongs to a different sales channel");
        }
    }

    public static SalesChannel createWebsite(Id id, Id merchantId, String name, Instant now) {
        Id requiredMerchantId = Objects.requireNonNull(merchantId, "merchantId is required");
        SalesChannel channel = new SalesChannel(
                id,
                name,
                ChannelType.WEBSITE,
                ChannelOwner.SELLER,
                requiredMerchantId,
                ChannelStatus.ENABLED,
                now,
                now,
                0,
                Set.of()
        );
        channel.addCreatedEvent(now);
        return channel;
    }

    public static SalesChannel createMarketplace(Id id, String name, Instant now) {
        SalesChannel channel = new SalesChannel(
                id,
                name,
                ChannelType.MARKETPLACE,
                ChannelOwner.PLATFORM,
                null,
                ChannelStatus.ENABLED,
                now,
                now,
                0,
                Set.of()
        );
        channel.addCreatedEvent(now);
        return channel;
    }

    public void enable(Instant now) {
        if (status == ChannelStatus.ENABLED) {
            return;
        }
        if (status != ChannelStatus.DISABLED) {
            throw invalidTransition(ChannelStatus.ENABLED.name());
        }
        this.status = ChannelStatus.ENABLED;
        this.updatedAt = now;
        addEvent(new SalesChannelEnabledEvent(
                getId().getValue(),
                type.name(),
                merchantIdValue(),
                now
        ));
    }

    public void disable(Instant now) {
        if (status == ChannelStatus.DISABLED) {
            return;
        }
        if (status != ChannelStatus.ENABLED) {
            throw invalidTransition(ChannelStatus.DISABLED.name());
        }
        this.status = ChannelStatus.DISABLED;
        this.updatedAt = now;
        addEvent(new SalesChannelDisabledEvent(
                getId().getValue(),
                type.name(),
                merchantIdValue(),
                now
        ));
    }

    public void requireEnabled() {
        if (status != ChannelStatus.ENABLED) {
            throw new SalesChannelDomainException(
                    new SalesChannelDomainError.ChannelDisabled(getId().getValue()),
                    "Sales channel is disabled"
            );
        }
    }

    public void requireOwnedBy(Id actingMerchantId) {
        if (owner == ChannelOwner.PLATFORM) {
            return;
        }
        if (merchantId == null || !merchantId.equals(actingMerchantId)) {
            String actingId = actingMerchantId == null ? "" : actingMerchantId.getValue();
            throw new SalesChannelDomainException(
                    new SalesChannelDomainError.ChannelOwnershipForbidden(getId().getValue(), actingId),
                    "Merchant does not own this sales channel"
            );
        }
    }

    public boolean isWebsite() {
        return type == ChannelType.WEBSITE;
    }

    public boolean isMarketplace() {
        return type == ChannelType.MARKETPLACE;
    }

    private void addCreatedEvent(Instant now) {
        addEvent(new SalesChannelCreatedEvent(
                getId().getValue(),
                name,
                type.name(),
                owner.name(),
                merchantIdValue(),
                status.name(),
                now
        ));
    }

    private void validateOwnership() {
        if (type == ChannelType.WEBSITE || type == ChannelType.POS) {
            if (owner != ChannelOwner.SELLER || merchantId == null) {
                throw new SalesChannelDomainException(
                        new SalesChannelDomainError.MerchantRequired(type.name()),
                        "Seller-owned channels require a merchant"
                );
            }
            return;
        }
        if (type == ChannelType.MARKETPLACE && (owner != ChannelOwner.PLATFORM || merchantId != null)) {
            throw new SalesChannelDomainException(
                    new SalesChannelDomainError.MarketplaceMustBePlatformOwned(),
                    "Marketplace channels are platform-owned"
            );
        }
    }

    private String requireName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return value;
    }

    private String merchantIdValue() {
        return merchantId == null ? null : merchantId.getValue();
    }

    private SalesChannelDomainException invalidTransition(String requested) {
        return new SalesChannelDomainException(
                new SalesChannelDomainError.InvalidStatusTransition(status.name(), requested),
                "Sales channel cannot perform this transition from " + status
        );
    }
}
