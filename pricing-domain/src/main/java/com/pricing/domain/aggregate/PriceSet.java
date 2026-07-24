package com.pricing.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.pricing.domain.entity.Price;
import com.pricing.domain.event.PriceSetCreatedEvent;
import com.pricing.domain.event.PriceSetUpdatedEvent;
import com.pricing.domain.exception.PricingDomainError;
import com.pricing.domain.exception.PricingDomainException;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
public class PriceSet extends AggregateRoot<Id> {
    private final List<Price> prices;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    public PriceSet(Id id, List<Price> prices, Instant createdAt, Instant updatedAt, long version) {
        super(id);
        this.prices = new ArrayList<>(prices == null ? List.of() : prices);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.version = version;
    }

    public static PriceSet create(Id id, Instant now) {
        PriceSet priceSet = new PriceSet(id, List.of(), now, now, 0);
        priceSet.addEvent(new PriceSetCreatedEvent(id.getValue(), now));
        return priceSet;
    }

    public List<Price> getPrices() {
        return Collections.unmodifiableList(prices);
    }

    public void addPrice(Price price, Instant now) {
        Objects.requireNonNull(price, "price is required");
        if (!price.getPriceSetId().equals(getId()) || !price.isBasePrice()) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("price"),
                    "price must belong to this price set as a base price"
            );
        }
        if (findPrice(price.getId()).isPresent()) {
            throw new PricingDomainException(
                    new PricingDomainError.DuplicatePrice(price.getId().getValue()),
                    "price already exists on price set"
            );
        }
        prices.add(price);
        touch(now);
    }

    public void replacePrice(Price price, Instant now) {
        Objects.requireNonNull(price, "price is required");
        int index = indexOfPrice(price.getId());
        if (index < 0) {
            throw new PricingDomainException(
                    new PricingDomainError.PriceNotFound(price.getId().getValue()),
                    "price not found on price set"
            );
        }
        prices.set(index, price);
        touch(now);
    }

    public void removePrice(Id priceId, Instant now) {
        int index = indexOfPrice(priceId);
        if (index < 0) {
            throw new PricingDomainException(
                    new PricingDomainError.PriceNotFound(priceId.getValue()),
                    "price not found on price set"
            );
        }
        prices.remove(index);
        touch(now);
    }

    public Optional<Price> findPrice(Id priceId) {
        return prices.stream().filter(price -> price.getId().equals(priceId)).findFirst();
    }

    private int indexOfPrice(Id priceId) {
        for (int i = 0; i < prices.size(); i++) {
            if (prices.get(i).getId().equals(priceId)) {
                return i;
            }
        }
        return -1;
    }

    private void touch(Instant now) {
        this.updatedAt = now;
        addEvent(new PriceSetUpdatedEvent(getId().getValue(), now));
    }
}
