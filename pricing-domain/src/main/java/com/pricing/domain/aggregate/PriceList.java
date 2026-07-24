package com.pricing.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.pricing.domain.entity.Price;
import com.pricing.domain.entity.PriceListRule;
import com.pricing.domain.enums.PriceListStatus;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.event.PriceListCreatedEvent;
import com.pricing.domain.event.PriceListUpdatedEvent;
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
public class PriceList extends AggregateRoot<Id> {
    private String title;
    private String description;
    private PriceListStatus status;
    private PriceListType type;
    private Instant startsAt;
    private Instant endsAt;
    private final List<PriceListRule> rules;
    private final List<Price> prices;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    public PriceList(
            Id id,
            String title,
            String description,
            PriceListStatus status,
            PriceListType type,
            Instant startsAt,
            Instant endsAt,
            List<PriceListRule> rules,
            List<Price> prices,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        super(id);
        this.rules = new ArrayList<>(rules == null ? List.of() : rules);
        this.prices = new ArrayList<>(prices == null ? List.of() : prices);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.version = version;
        replaceMetadata(title, description, status, type, startsAt, endsAt);
    }

    public static PriceList create(
            Id id,
            String title,
            String description,
            PriceListType type,
            Instant startsAt,
            Instant endsAt,
            Instant now
    ) {
        PriceList priceList = new PriceList(
                id,
                title,
                description,
                PriceListStatus.DRAFT,
                type == null ? PriceListType.SALE : type,
                startsAt,
                endsAt,
                List.of(),
                List.of(),
                now,
                now,
                0
        );
        priceList.addEvent(new PriceListCreatedEvent(id.getValue(), now));
        return priceList;
    }

    public void replaceMetadata(
            String title,
            String description,
            PriceListStatus status,
            PriceListType type,
            Instant startsAt,
            Instant endsAt
    ) {
        validateWindow(startsAt, endsAt);
        if (title == null || title.isBlank()) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("title"),
                    "title is required"
            );
        }
        this.title = title.trim();
        this.description = description;
        this.status = Objects.requireNonNull(status, "status is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void update(
            String title,
            String description,
            PriceListStatus status,
            PriceListType type,
            Instant startsAt,
            Instant endsAt,
            Instant now
    ) {
        replaceMetadata(title, description, status, type, startsAt, endsAt);
        touch(now);
    }

    public void replaceRules(List<PriceListRule> nextRules, Instant now) {
        rules.clear();
        if (nextRules != null) {
            rules.addAll(nextRules);
        }
        touch(now);
    }

    public void addPrice(Price price, Instant now) {
        Objects.requireNonNull(price, "price is required");
        if (price.getPriceListId().isEmpty() || !price.getPriceListId().get().equals(getId())) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("price"),
                    "price must belong to this price list"
            );
        }
        if (findPrice(price.getId()).isPresent()) {
            throw new PricingDomainException(
                    new PricingDomainError.DuplicatePrice(price.getId().getValue()),
                    "price already exists on price list"
            );
        }
        prices.add(price);
        touch(now);
    }

    public void replacePrice(Price price, Instant now) {
        int index = indexOfPrice(price.getId());
        if (index < 0) {
            throw new PricingDomainException(
                    new PricingDomainError.PriceNotFound(price.getId().getValue()),
                    "price not found on price list"
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
                    "price not found on price list"
            );
        }
        prices.remove(index);
        touch(now);
    }

    public int rulesCount() {
        return rules.size();
    }

    public List<PriceListRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public List<Price> getPrices() {
        return Collections.unmodifiableList(prices);
    }

    public Optional<Price> findPrice(Id priceId) {
        return prices.stream().filter(price -> price.getId().equals(priceId)).findFirst();
    }

    public boolean isActiveAt(Instant now) {
        if (status != PriceListStatus.ACTIVE) {
            return false;
        }
        boolean started = startsAt == null || !startsAt.isAfter(now);
        boolean notEnded = endsAt == null || !endsAt.isBefore(now);
        return started && notEnded;
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
        addEvent(new PriceListUpdatedEvent(getId().getValue(), now));
    }

    private static void validateWindow(Instant startsAt, Instant endsAt) {
        if (startsAt != null && endsAt != null && startsAt.isAfter(endsAt)) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidPriceListWindow(
                            startsAt.toString(),
                            endsAt.toString()
                    ),
                    "startsAt must be before endsAt"
            );
        }
    }
}
