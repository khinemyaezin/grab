package com.grab.app.channeldiscovery.saleschannel.internal.adapter;

import com.grab.framework.id.Id;
import com.grab.app.channeldiscovery.region.port.RegionQuery;
import com.saleschannel.application.port.outbound.RegionStatusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionStatusAdapter implements RegionStatusPort {
    private final RegionQuery regionQuery;

    @Override
    public boolean isActive(Id regionId) {
        return regionQuery.find(regionId.getValue()).map(RegionQuery.RegionSlice::active).orElse(false);
    }
}
