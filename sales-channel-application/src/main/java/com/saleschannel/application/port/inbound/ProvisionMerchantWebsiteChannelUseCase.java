package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;

public interface ProvisionMerchantWebsiteChannelUseCase {
    ProvisionSalesChannelResult execute(ProvisionMerchantWebsiteChannelCommand command);
}
