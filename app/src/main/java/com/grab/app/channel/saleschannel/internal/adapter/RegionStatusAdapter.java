package com.grab.app.channel.saleschannel.internal.adapter;

import com.grab.app.channel.region.port.message.RegionPort;
import com.grab.app.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.framework.id.Id;
import com.saleschannel.application.port.outbound.RegionStatusPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class RegionStatusAdapter implements RegionStatusPort {

    private final RegionPort regionPort;

    public RegionStatusAdapter(RegionPort regionPort) {
        this.regionPort = regionPort;
    }

    @Override
    @SalesChannelTransactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean isActive(Id regionId) {
        if (regionId == null) {
            return false;
        }
        return regionPort.findRegionById(regionId)
                .map(RegionPort.RegionMessageResult::active)
                .orElse(false);
    }
}

