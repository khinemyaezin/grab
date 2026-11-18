package com.grab.service.account.merchant.internal.adapter.outbound.message;

import com.grab.rpc.adapter.RmiClientFactory;
import com.grab.service.account.merchant.internal.config.MerchantTransactional;
import com.grab.service.contract.message.channel.region.RegionPort;
import com.merchant.application.port.outbound.PrimaryMarketRegionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.rmi.RemoteException;

@Component
public class PrimaryMarketRegionAdapter implements PrimaryMarketRegionPort {

    private final RmiClientFactory clients;

    public PrimaryMarketRegionAdapter(RmiClientFactory clients) {
        this.clients = clients;
    }

    @Override
    @MerchantTransactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean hasActiveRegion(String countryCode) {
        if (countryCode == null) {
            return false;
        }
        RegionPort regions = clients.create(RegionPort.class, "channel");
        try {
            RegionPort.RegionMessageResult region = regions.findRegionByCountryCode(countryCode);
            return region != null && region.active();
        } catch (RemoteException exception) {
            throw new IllegalStateException("Unable to look up the primary market region", exception);
        }
    }
}
