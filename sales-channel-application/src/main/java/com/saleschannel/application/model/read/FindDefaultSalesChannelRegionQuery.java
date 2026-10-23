package com.saleschannel.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.Optional;

public record FindDefaultSalesChannelRegionQuery(Id salesChannelId) implements Query<Optional<Id>> {
}
