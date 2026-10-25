package com.grab.app.channeldiscovery.region.port;

import java.util.Optional;
import java.util.List;

public interface RegionQuery {

    Optional<RegionSlice> find(String regionId);

    Optional<RegionSlice> findByCountry(String countryCode);

    List<String> findActiveCountryCodes();

    record RegionSlice(String regionId, String currencyCode, String status) {
        public boolean active() {
            return "ACTIVE".equals(status);
        }
    }
}
