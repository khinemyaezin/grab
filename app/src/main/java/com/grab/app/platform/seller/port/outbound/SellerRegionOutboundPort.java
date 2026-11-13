package com.grab.app.platform.seller.port.outbound;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Seller Outbound Port for Geographic Regions, Currencies, and Countries.
 */
public interface SellerRegionOutboundPort {

    Optional<RegionOutboundResult> findRegionById(String regionId);

    Optional<RegionOutboundResult> findRegionByCountryCode(String countryCode);

    List<String> listSupportedCountryCodes();

    record RegionOutboundResult(
            String regionId,
            String name,
            String currencyCode,
            String status,
            Set<String> countryCodes
    ) {}
}

