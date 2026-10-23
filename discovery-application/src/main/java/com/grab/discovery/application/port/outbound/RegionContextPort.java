package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.Optional;

public interface RegionContextPort {
    Optional<RegionView> findByCountry(String countryCode);

    Optional<RegionView> findById(Id regionId);

    record RegionView(
            Id regionId,
            String currencyCode,
            boolean active
    ) {
    }
}
