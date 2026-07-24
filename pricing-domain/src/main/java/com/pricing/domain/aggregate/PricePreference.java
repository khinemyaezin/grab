package com.pricing.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.pricing.domain.event.PricePreferenceCreatedEvent;
import com.pricing.domain.event.PricePreferenceUpdatedEvent;
import com.pricing.domain.exception.PricingDomainError;
import com.pricing.domain.exception.PricingDomainException;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class PricePreference extends AggregateRoot<Id> {
    private String attribute;
    private String value;
    private boolean taxInclusive;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    public PricePreference(
            Id id,
            String attribute,
            String value,
            boolean taxInclusive,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        super(id);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.version = version;
        replace(attribute, value, taxInclusive);
    }

    public static PricePreference create(Id id, String attribute, String value, boolean taxInclusive, Instant now) {
        PricePreference preference = new PricePreference(id, attribute, value, taxInclusive, now, now, 0);
        preference.addEvent(new PricePreferenceCreatedEvent(id.getValue(), now));
        return preference;
    }

    public void update(String attribute, String value, boolean taxInclusive, Instant now) {
        replace(attribute, value, taxInclusive);
        this.updatedAt = now;
        addEvent(new PricePreferenceUpdatedEvent(getId().getValue(), now));
    }

    private void replace(String attribute, String value, boolean taxInclusive) {
        if (attribute == null || attribute.isBlank()) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("attribute"),
                    "attribute is required"
            );
        }
        this.attribute = attribute.trim();
        this.value = value;
        this.taxInclusive = taxInclusive;
    }
}
