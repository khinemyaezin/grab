package com.grab.app.channel.saleschannel.internal.adapter;

import com.grab.app.channel.region.port.message.RegionPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.saleschannel.application.port.outbound.PrimaryMarketRegionPort;
import org.springframework.stereotype.Component;

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
    public Optional<Id> findActiveRegionByCountry(String countryCode) {
        if (countryCode == null) {
            return Optional.empty();
        }
        return regionPort.findRegionByCountryCode(countryCode)
                .filter(RegionPort.RegionMessageResult::active)
                .map(res -> idGenerator.convertIdFrom(res.regionId()));
    }
}

