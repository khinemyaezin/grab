package com.grab.store.saleschannel.internal.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.region.port.RegionQuery;
import com.saleschannel.application.port.outbound.PrimaryMarketRegionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("saleChannelPrimaryMarketRegionAdapter")
@RequiredArgsConstructor
public class PrimaryMarketRegionAdapter implements PrimaryMarketRegionPort {
    private final RegionQuery regions;
    private final IdGenerator ids;

    @Override
    public Optional<Id> findActiveRegionByCountry(String countryCode) {
        return regions.findByCountry(countryCode)
                .filter(RegionQuery.RegionSlice::active)
                .map(slice -> ids.convertIdFrom(slice.regionId()));
    }
}
