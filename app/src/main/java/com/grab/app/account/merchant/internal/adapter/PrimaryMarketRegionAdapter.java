package com.grab.app.account.merchant.internal.adapter;

import com.grab.app.channel.region.port.message.RegionPort;
import com.merchant.application.port.outbound.PrimaryMarketRegionPort;
import org.springframework.stereotype.Component;

@Component("merchantPrimaryMarketRegionAdapter")
public class PrimaryMarketRegionAdapter implements PrimaryMarketRegionPort {

    private final RegionPort regionPort;

    public PrimaryMarketRegionAdapter(RegionPort regionPort) {
        this.regionPort = regionPort;
    }

    @Override
    public boolean hasActiveRegion(String countryCode) {
        if (countryCode == null) {
            return false;
        }
        return regionPort.findRegionByCountryCode(countryCode)
                .map(RegionPort.RegionMessageResult::active)
                .orElse(false);
    }
}
