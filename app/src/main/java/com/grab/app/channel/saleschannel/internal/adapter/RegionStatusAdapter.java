package com.grab.app.channel.saleschannel.internal.adapter;

import com.grab.app.channel.region.port.message.RegionPort;
import com.grab.framework.id.Id;
import com.saleschannel.application.port.outbound.RegionStatusPort;
import org.springframework.stereotype.Component;

@Component
public class RegionStatusAdapter implements RegionStatusPort {

    private final RegionPort regionPort;

    public RegionStatusAdapter(RegionPort regionPort) {
        this.regionPort = regionPort;
    }

    @Override
    public boolean isActive(Id regionId) {
        if (regionId == null) {
            return false;
        }
        return regionPort.findRegionById(regionId)
                .map(RegionPort.RegionMessageResult::active)
                .orElse(false);
    }
}

