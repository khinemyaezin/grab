package com.grab.app.channel.region.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Universal Java Port Interface for Geographic Regions, Currencies, and Jurisdictions.
 * Unifies RegionController and RegionQuery.
 */
public interface RegionPort {

    Optional<RegionMessageResult> findRegionById(Id regionId);

    Optional<RegionMessageResult> findRegionByCountryCode(String countryCode);

    List<String> listSupportedCountryCodes();

    record RegionMessageResult(
            String regionId,
            String name,
            String currencyCode,
            String status,
            Set<String> countryCodes
    ) {
        public boolean active() {
            return "ACTIVE".equalsIgnoreCase(status);
        }
    }
}
