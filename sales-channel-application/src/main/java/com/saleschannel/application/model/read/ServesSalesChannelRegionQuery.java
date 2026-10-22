package com.saleschannel.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record ServesSalesChannelRegionQuery(Id salesChannelId, Id regionId) implements Query<Boolean> {
}
