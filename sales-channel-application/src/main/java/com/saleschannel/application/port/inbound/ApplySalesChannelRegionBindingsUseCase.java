package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.write.ApplySalesChannelRegionBindingsCommand;

public interface ApplySalesChannelRegionBindingsUseCase {
    Integer execute(ApplySalesChannelRegionBindingsCommand command);
}
