package com.grab.store.region.query;

import java.util.Optional;

public interface RegionQueryPort {

    Optional<RegionSlice> find(String regionId);

    record RegionSlice(String regionId, String currencyCode, String status) {
        public boolean active() {
            return "ACTIVE".equals(status);
        }
    }
}
