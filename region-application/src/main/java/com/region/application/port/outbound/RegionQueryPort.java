package com.region.application.port.outbound;

import com.region.application.model.read.RegionView;

import java.util.Optional;

public interface RegionQueryPort {
    Optional<RegionView> findById(String regionId);

    boolean countryAllowed(String regionId, String countryCode);
}
