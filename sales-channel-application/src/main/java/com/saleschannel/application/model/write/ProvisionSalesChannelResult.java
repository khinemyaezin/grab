package com.saleschannel.application.model.write;

import com.grab.framework.id.Id;

public record ProvisionSalesChannelResult(
        Id salesChannelId,
        boolean created
) {
}
