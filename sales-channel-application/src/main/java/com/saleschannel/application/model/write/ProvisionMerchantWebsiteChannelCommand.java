package com.saleschannel.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ProvisionMerchantWebsiteChannelCommand(
        Id merchantId
) implements Command<ProvisionSalesChannelResult> {
}
