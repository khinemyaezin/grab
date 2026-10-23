package com.saleschannel.application.port.inbound;

import com.grab.framework.id.Id;

import java.util.List;

public interface ListEnabledSalesChannelRegionsUseCase {
    List<Id> execute(Id salesChannelId);
}
