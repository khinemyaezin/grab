package com.grab.service.channel.saleschannel.internal.adapter;

import com.grab.service.contract.message.channel.region.RegionPort;
import com.grab.service.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.saleschannel.application.port.outbound.PrimaryMarketRegionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.rmi.RemoteException;
import java.util.Optional;

@Component("salesChannelPrimaryMarketRegionAdapter")
public class PrimaryMarketRegionAdapter implements PrimaryMarketRegionPort {

    private final RegionPort regionPort;
    private final IdGenerator idGenerator;

    public PrimaryMarketRegionAdapter(RegionPort regionPort, IdGenerator idGenerator) {
        this.regionPort = regionPort;
        this.idGenerator = idGenerator;
    }

    @Override
    @SalesChannelTransactional(propagation = Propagation.NOT_SUPPORTED)
    public Optional<Id> findActiveRegionByCountry(String countryCode) {
        if (countryCode == null) {
            return Optional.empty();
        }
        try {
            var region = regionPort.findRegionByCountryCode(countryCode);
            return Optional.ofNullable(region)
                    .filter(RegionPort.RegionMessageResult::active)
                    .map(result -> idGenerator.convertIdFrom(result.regionId()));
        } catch (RemoteException exception) {
            throw new IllegalStateException("Local region lookup failed", exception);
        }
    }
}

