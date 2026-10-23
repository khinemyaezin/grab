package com.grab.store.saleschannel.port;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public interface ChannelRegionQueryPort {
    boolean servesRegion(Id salesChannelId, Id regionId);

    Optional<Id> findDefaultRegion(Id salesChannelId);

    List<Id> findEnabledRegions(Id salesChannelId);
}
