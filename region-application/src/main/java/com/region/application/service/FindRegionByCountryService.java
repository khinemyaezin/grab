package com.region.application.service;

import com.region.application.model.read.FindRegionByCountryQuery;
import com.region.application.model.read.RegionResult;
import com.region.application.port.inbound.FindRegionByCountryUseCase;
import com.region.application.port.outbound.RegionQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FindRegionByCountryService implements FindRegionByCountryUseCase {
    private final RegionQueryPort regionQueryPort;

    @Override
    public Optional<RegionResult> execute(FindRegionByCountryQuery query) {
        String countryCode = query.countryCode().trim().toUpperCase();
        return regionQueryPort.findByCountry(countryCode).map(view -> new RegionResult(
                view.id(),
                view.name(),
                view.currencyCode(),
                view.status(),
                view.countryCodes()
        ));
    }
}
