package com.pricing.domain.repository;

import com.grab.framework.id.Id;
import com.pricing.domain.aggregate.PriceList;

import java.util.List;
import java.util.Optional;

public interface PriceListRepository {
    Optional<PriceList> findById(Id id);

    List<PriceList> findAll();

    PriceList save(PriceList priceList);

    void delete(Id id);
}
