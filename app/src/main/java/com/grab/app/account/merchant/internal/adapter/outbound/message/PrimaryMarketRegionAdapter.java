package com.grab.app.account.merchant.internal.adapter.outbound.message;

import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.app.channel.region.port.message.RegionPort;
import com.merchant.application.port.outbound.PrimaryMarketRegionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component("merchantPrimaryMarketRegionAdapter")
public class PrimaryMarketRegionAdapter implements PrimaryMarketRegionPort {

    private final RegionPort regionPort;

    public PrimaryMarketRegionAdapter(RegionPort regionPort) {
        this.regionPort = regionPort;
    }

    @Override
    @MerchantTransactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean hasActiveRegion(String countryCode) {
        if (countryCode == null) {
            return false;
        }
        return regionPort.findRegionByCountryCode(countryCode)
                .map(RegionPort.RegionMessageResult::active)
                .orElse(false);
    }
}
