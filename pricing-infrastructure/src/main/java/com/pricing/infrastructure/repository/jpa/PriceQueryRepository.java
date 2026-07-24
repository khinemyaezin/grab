package com.pricing.infrastructure.repository.jpa;

import com.grab.framework.id.Id;
import com.pricing.domain.policy.PriceCandidate;
import com.pricing.domain.policy.PricePreferenceView;

import java.util.Collection;
import java.util.List;

public interface PriceQueryRepository {
    List<PriceCandidate> findCandidates(Collection<Id> priceSetIds, String currencyCode);

    List<PricePreferenceView> findPreferences();
}
