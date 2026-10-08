package com.grab.store.region.port;

import java.util.Optional;

public interface RegionQuery {

    Optional<RegionSlice> find(String regionId);

    record RegionSlice(String regionId, String currencyCode, String status) {
        public boolean active() {
            return "ACTIVE".equals(status);
        }
    }
}
