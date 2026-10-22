package com.saleschannel.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public interface ChannelRegionLookupPort {
    boolean servesRegion(Id salesChannelId, Id regionId);

    Optional<Id> findDefaultRegion(Id salesChannelId);

    List<Id> findEnabledRegions(Id salesChannelId);
}
