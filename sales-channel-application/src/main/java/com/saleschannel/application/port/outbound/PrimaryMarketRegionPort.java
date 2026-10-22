package com.saleschannel.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.Optional;

public interface PrimaryMarketRegionPort {
    Optional<Id> findActiveRegionByCountry(String countryCode);
}
