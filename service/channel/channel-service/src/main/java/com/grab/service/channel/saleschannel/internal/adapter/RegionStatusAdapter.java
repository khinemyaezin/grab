package com.grab.service.channel.saleschannel.internal.adapter;

import com.grab.service.contract.message.channel.region.RegionPort;
import com.grab.service.channel.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.framework.id.Id;
import com.saleschannel.application.port.outbound.RegionStatusPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.rmi.RemoteException;

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
        try {
            var region = regionPort.findRegionById(regionId);
            return region != null && region.active();
        } catch (RemoteException exception) {
            throw new IllegalStateException("Local region lookup failed", exception);
        }
    }
}

