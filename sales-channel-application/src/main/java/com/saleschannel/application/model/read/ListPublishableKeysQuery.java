package com.saleschannel.application.model.read;

import com.grab.framework.cqrs.query.PageableQueryRequest;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Objects;

public record ListPublishableKeysQuery(Id salesChannelId, PublishableKeyManagementAccess access, Pageable pageable)
        implements Query<Page<PublishableKeyView>>, PageableQueryRequest {
    public ListPublishableKeysQuery {
        Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        Objects.requireNonNull(pageable, "pageable is required");
    }
}
