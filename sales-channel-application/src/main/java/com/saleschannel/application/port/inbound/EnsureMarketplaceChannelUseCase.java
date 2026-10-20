package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.write.EnsureMarketplaceChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;

public interface EnsureMarketplaceChannelUseCase {
    ProvisionSalesChannelResult execute(EnsureMarketplaceChannelCommand command);
}
