package com.saleschannel.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record EnsureMarketplaceChannelCommand() implements Command<ProvisionSalesChannelResult> {
}
