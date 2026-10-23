package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.Optional;

public interface SalesChannelRegionPort {
    Optional<Id> findDefaultRegion(Id salesChannelId);

    boolean servesRegion(Id salesChannelId, Id regionId);
}
