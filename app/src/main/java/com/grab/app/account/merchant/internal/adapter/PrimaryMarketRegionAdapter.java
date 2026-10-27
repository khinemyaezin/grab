package com.grab.app.account.merchant.internal.adapter;

import com.grab.app.channel.region.port.RegionQuery;
import com.merchant.application.port.outbound.PrimaryMarketRegionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("merchantPrimaryMarketRegionAdapter")
@RequiredArgsConstructor
public class PrimaryMarketRegionAdapter implements PrimaryMarketRegionPort {
    private final RegionQuery regions;

    @Override
    public boolean hasActiveRegion(String countryCode) {
        return regions.findByCountry(countryCode).map(RegionQuery.RegionSlice::active).orElse(false);
    }
}
