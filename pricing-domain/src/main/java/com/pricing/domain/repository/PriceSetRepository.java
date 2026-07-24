package com.pricing.domain.repository;

import com.grab.framework.id.Id;
import com.pricing.domain.aggregate.PriceSet;

import java.util.Optional;

public interface PriceSetRepository {
    Optional<PriceSet> findById(Id id);

    PriceSet save(PriceSet priceSet);

    void delete(Id id);
}
