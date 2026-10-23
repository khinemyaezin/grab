package com.saleschannel.application.port.inbound;

import com.grab.framework.id.Id;

public interface ServesSalesChannelRegionUseCase {
    boolean execute(Id salesChannelId, Id regionId);
}
