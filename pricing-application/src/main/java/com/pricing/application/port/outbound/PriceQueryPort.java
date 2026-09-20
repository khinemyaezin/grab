package com.pricing.application.port.outbound;

import com.grab.framework.id.Id;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.domain.policy.PriceCandidate;
import com.pricing.domain.policy.PricePreferenceView;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PriceQueryPort {
    List<PriceCandidate> findCandidates(Collection<Id> priceSetIds, String currencyCode);

    List<PricePreferenceView> findPreferences();

    Optional<PriceSetResult> findPriceSetById(String priceSetId);

    Optional<PriceListResult> findPriceListById(String priceListId);

    Optional<PricePreferenceResult> findPricePreferenceById(String pricePreferenceId);

    List<PriceListResult> findAllPriceLists();
}
