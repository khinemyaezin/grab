package com.grab.app.channel.region.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Geographic Regions, Currencies, and Jurisdictions.
 * Unifies RegionController and RegionQuery.
 */
public interface RegionPort {

    Optional<RegionSliceResult> findRegionById(Id regionId);

    Optional<RegionSliceResult> findRegionByCountryCode(String countryCode);

    List<SupportedCountryResult> listSupportedCountries();

    List<RegionSliceResult> listRegions();

    record RegionSliceResult(
            Id regionId,
            String name,
            String currencyCode,
            BigDecimal taxRate,
            List<String> countryCodes
    ) {}

    record SupportedCountryResult(
            String countryCode,
            String countryName,
            Id regionId
    ) {}
}

