package com.pricing.application.service;

import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.port.inbound.ListPricePreferencesUseCase;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.application.model.read.ListPricePreferencesQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListPricePreferencesService implements ListPricePreferencesUseCase {

    private final PriceQueryPort priceQueryPort;

    public List<PricePreferenceResult> execute(ListPricePreferencesQuery query) {
        return priceQueryPort.findPreferences().stream()
                .map(view -> new PricePreferenceResult(
                        view.id().getValue(),
                        view.attribute(),
                        view.value(),
                        view.taxInclusive()
                ))
                .toList();
    }
}
