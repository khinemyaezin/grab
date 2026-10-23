package com.region.application.service;

import com.region.application.model.read.ListActiveCountryCodesQuery;
import com.region.application.port.inbound.ListActiveCountryCodesUseCase;
import com.region.application.port.outbound.RegionQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListActiveCountryCodesService implements ListActiveCountryCodesUseCase {
    private final RegionQueryPort regions;

    @Override
    public List<String> execute(ListActiveCountryCodesQuery query) {
        return regions.findActiveCountryCodes();
    }
}
