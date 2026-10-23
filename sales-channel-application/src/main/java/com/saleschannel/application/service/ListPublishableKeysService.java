package com.saleschannel.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.saleschannel.application.exception.SalesChannelServiceError;
import com.saleschannel.application.exception.SalesChannelServiceException;
import com.saleschannel.application.model.read.ListPublishableKeysQuery;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.model.read.SalesChannelView;
import com.saleschannel.application.port.inbound.ListPublishableKeysUseCase;
import com.saleschannel.application.port.outbound.PublishableKeyQueryPort;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import com.saleschannel.domain.policy.PublishableKeyManagementPolicy;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ListPublishableKeysService implements ListPublishableKeysUseCase {
    private static final Logger log = Loggers.getLogger(ListPublishableKeysService.class);
    private final SalesChannelQueryPort channels;
    private final PublishableKeyQueryPort keys;
    private final IdGenerator ids;
    private final PublishableKeyManagementPolicy managementPolicy;

    @Override
    public Page<PublishableKeyView> execute(ListPublishableKeysQuery query) {
        Id salesChannelId = query.salesChannelId();
        String channelId = salesChannelId.getValue();
        SalesChannelView channel = channels.findById(channelId)
                .orElseThrow(() -> channelNotFound(channelId));
        String merchantId = channel.merchantId();
        Id owningMerchantId = merchantId == null ? null : ids.convertIdFrom(merchantId);
        ChannelOwner owner = channel.owner();
        PublishableKeyManagementAccess access = query.access();
        managementPolicy.requireAllowed(salesChannelId, owner, owningMerchantId, access);
        Pageable pageable = query.pageable();
        log.debug("Listing publishable key metadata for salesChannelId={}", channelId);
        return keys.list(salesChannelId, pageable);
    }

    private SalesChannelServiceException channelNotFound(String channelId) {
        SalesChannelServiceError error = new SalesChannelServiceError.ChannelNotFound(channelId);
        return new SalesChannelServiceException(error, "Sales channel not found");
    }
}
