package com.saleschannel.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.saleschannel.application.exception.SalesChannelServiceError;
import com.saleschannel.application.exception.SalesChannelServiceException;
import com.saleschannel.application.model.write.RevokePublishableKeyCommand;
import com.saleschannel.application.port.inbound.RevokePublishableKeyUseCase;
import com.saleschannel.domain.aggregate.PublishableKey;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.policy.PublishableKeyManagementPolicy;
import com.saleschannel.domain.port.outbound.PublishableKeyRepository;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
public class RevokePublishableKeyService implements RevokePublishableKeyUseCase {
    private static final Logger log = Loggers.getLogger(RevokePublishableKeyService.class);
    private final SalesChannelRepository channels;
    private final PublishableKeyRepository keys;
    private final Clock clock;
    private final PublishableKeyManagementPolicy managementPolicy;

    @Override
    public Void execute(RevokePublishableKeyCommand command) {
        Id channelId = command.salesChannelId();
        Id keyId = command.keyId();
        SalesChannel channel = channels.findById(channelId)
                .orElseThrow(() -> channelNotFound(channelId));
        ChannelOwner owner = channel.getOwner();
        Id owningMerchantId = channel.getMerchantId();
        PublishableKeyManagementAccess access = command.access();
        managementPolicy.requireAllowed(channelId, owner, owningMerchantId, access);
        PublishableKey key = keys.findById(keyId)
                .filter(candidate -> candidate.getSalesChannelId().equals(channelId))
                .orElseThrow(() -> keyNotFound(keyId));
        String keyValue = keyId.getValue();
        String channelValue = channelId.getValue();
        if (key.isActive()) {
            Instant now = clock.instant();
            key.revoke(now);
            keys.save(key);
            log.info("Revoked publishable key keyId={}, salesChannelId={}", keyValue, channelValue);
        } else {
            log.debug("Publishable key already revoked keyId={}, salesChannelId={}", keyValue, channelValue);
        }
        return null;
    }

    private SalesChannelServiceException channelNotFound(Id channelId) {
        String value = channelId.getValue();
        SalesChannelServiceError error = new SalesChannelServiceError.ChannelNotFound(value);
        return new SalesChannelServiceException(error, "Sales channel not found");
    }

    private SalesChannelServiceException keyNotFound(Id keyId) {
        SalesChannelServiceError error = new SalesChannelServiceError.PublishableKeyNotFound(keyId);
        return new SalesChannelServiceException(error, "Publishable key not found");
    }
}
