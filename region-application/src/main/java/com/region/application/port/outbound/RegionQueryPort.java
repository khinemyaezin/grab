package com.region.application.port.outbound;

import com.region.application.model.read.RegionView;

import java.util.Optional;
import java.util.List;

public interface RegionQueryPort {
    Optional<RegionView> findById(String regionId);

    boolean countryAllowed(String regionId, String countryCode);

    Optional<RegionView> findByCountry(String countryCode);

    List<String> findActiveCountryCodes();
}
