package com.merchant.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.merchant.domain.enums.StorefrontStatus;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import com.merchant.domain.event.StorefrontStatusChangedEvent;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.valueobject.LifecycleReason;
import com.merchant.domain.valueobject.StorefrontName;
import com.merchant.domain.valueobject.StorefrontSlug;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Storefront extends AggregateRoot<Id> {
    private final Id merchantId;
    private StorefrontName name;
    private StorefrontSlug slug;
    private StorefrontStatus status;
    private LifecycleReason lifecycleReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    public Storefront(
            Id id,
            Id merchantId,
            StorefrontName name,
            StorefrontSlug slug,
            StorefrontStatus status,
            LifecycleReason lifecycleReason,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        super(id);
        this.merchantId = Objects.requireNonNull(merchantId, "merchantId is required");
        this.name = Objects.requireNonNull(name, "storefront name is required");
        this.slug = Objects.requireNonNull(slug, "storefront slug is required");
        this.status = Objects.requireNonNull(status, "storefront status is required");
        this.lifecycleReason = lifecycleReason;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.version = version;
    }

    public static Storefront createDraft(
            Id id,
            Id merchantId,
            StorefrontName name,
            StorefrontSlug slug,
            Instant now
    ) {
        Storefront storefront = new Storefront(
                id,
                merchantId,
                name,
                slug,
                StorefrontStatus.DRAFT,
                null,
                now,
                now,
                0
        );
        storefront.addEvent(new StorefrontCreatedEvent(
                id.getValue(),
                merchantId.getValue(),
                name.value(),
                slug.value(),
                StorefrontStatus.DRAFT.name(),
                1,
                now
        ));
        return storefront;
    }

    public void rename(StorefrontName name, StorefrontSlug slug, Instant now) {
        requireNotClosed();
        StorefrontName previousName = this.name;
        StorefrontSlug previousSlug = this.slug;
        this.name = Objects.requireNonNull(name, "storefront name is required");
        this.slug = Objects.requireNonNull(slug, "storefront slug is required");
        this.updatedAt = now;
        addEvent(new StorefrontRenamedEvent(
                getId().getValue(),
                merchantId.getValue(),
                previousName.value(),
                this.name.value(),
                previousSlug.value(),
                this.slug.value(),
                version + 1,
                now
        ));
    }

    public void activate(Instant now) {
        requireStatus(StorefrontStatus.DRAFT);
        transition(StorefrontStatus.ACTIVE, null, now);
    }

    public void suspend(LifecycleReason reason, Instant now) {
        requireStatus(StorefrontStatus.ACTIVE);
        transition(StorefrontStatus.SUSPENDED, Objects.requireNonNull(reason, "lifecycle reason is required"), now);
    }

    public void reactivate(Instant now) {
        requireStatus(StorefrontStatus.SUSPENDED);
        transition(StorefrontStatus.ACTIVE, null, now);
    }

    public void close(LifecycleReason reason, Instant now) {
        requireStatus(StorefrontStatus.DRAFT, StorefrontStatus.ACTIVE, StorefrontStatus.SUSPENDED);
        transition(StorefrontStatus.CLOSED, Objects.requireNonNull(reason, "lifecycle reason is required"), now);
    }

    private void transition(StorefrontStatus target, LifecycleReason reason, Instant now) {
        StorefrontStatus previous = this.status;
        this.status = target;
        this.lifecycleReason = reason;
        this.updatedAt = now;
        addEvent(new StorefrontStatusChangedEvent(
                getId().getValue(),
                merchantId.getValue(),
                slug.value(),
                previous.name(),
                target.name(),
                version + 1,
                now
        ));
    }

    private void requireNotClosed() {
        if (status == StorefrontStatus.CLOSED) {
            throw invalidTransition(StorefrontStatus.CLOSED.name());
        }
    }

    private void requireStatus(StorefrontStatus... allowed) {
        for (StorefrontStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        String requested = allowed.length == 1 ? allowed[0].name() : "allowed lifecycle transition";
        throw invalidTransition(requested);
    }

    private MerchantDomainException invalidTransition(String requested) {
        return new MerchantDomainException(
                new MerchantDomainError.InvalidStorefrontTransition(status.name(), requested),
                "Storefront cannot perform this transition from " + status
        );
    }
}
