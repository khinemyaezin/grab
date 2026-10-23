package com.grab.store.saleschannel.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.saleschannel.internal.api.rest.dto.response.PublishableKeyResponse;
import com.grab.store.saleschannel.internal.api.rest.mapper.PublishableKeyDtoMapper;
import com.grab.store.saleschannel.internal.exception.SalesChannelServiceError;
import com.grab.store.saleschannel.internal.exception.SalesChannelServiceException;
import com.saleschannel.application.model.read.ListPublishableKeysQuery;
import com.saleschannel.application.model.read.PublishableKeyView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublishableKeyQueryService {
    private final QueryBus queryBus;
    private final PublishableKeyDtoMapper mapper;
    private final PublishableKeyAccessResolver accessResolver;

    public Page<PublishableKeyResponse> list(String salesChannelId, Pageable pageable) {
        validateSort(pageable);
        com.saleschannel.domain.valueobject.PublishableKeyManagementAccess access = accessResolver.resolve();
        ListPublishableKeysQuery query = new ListPublishableKeysQuery(
                new com.grab.framework.id.impl.CommonId(salesChannelId), access, pageable);
        Page<PublishableKeyView> result = queryBus.dispatch(query);
        return result.map(mapper::toResponse);
    }

    private void validateSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!isSupported(order.getProperty())) {
                SalesChannelServiceError error =
                        new SalesChannelServiceError.UnsupportedPublishableKeySort(order.getProperty());
                throw new SalesChannelServiceException(error, "Unsupported publishable key sort");
            }
        }
    }

    private boolean isSupported(String property) {
        return "keyId".equals(property)
                || "label".equals(property)
                || "createdAt".equals(property)
                || "revokedAt".equals(property);
    }
}
