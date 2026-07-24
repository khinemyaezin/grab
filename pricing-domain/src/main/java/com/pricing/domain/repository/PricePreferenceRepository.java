package com.pricing.domain.repository;

import com.grab.framework.id.Id;
import com.pricing.domain.aggregate.PricePreference;

import java.util.List;
import java.util.Optional;

public interface PricePreferenceRepository {
    Optional<PricePreference> findById(Id id);

    List<PricePreference> findAll();

    PricePreference save(PricePreference preference);

    void delete(Id id);
}
