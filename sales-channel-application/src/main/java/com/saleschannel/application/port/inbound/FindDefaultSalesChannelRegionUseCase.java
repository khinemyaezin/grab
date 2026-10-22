package com.saleschannel.application.port.inbound;

import com.grab.framework.id.Id;

import java.util.Optional;

public interface FindDefaultSalesChannelRegionUseCase {
    Optional<Id> execute(Id salesChannelId);
}
