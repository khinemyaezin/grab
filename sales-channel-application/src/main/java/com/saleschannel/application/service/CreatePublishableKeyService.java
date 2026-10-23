package com.saleschannel.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.saleschannel.application.exception.SalesChannelServiceError;
import com.saleschannel.application.exception.SalesChannelServiceException;
import com.saleschannel.application.model.write.CreatePublishableKeyCommand;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;
import com.saleschannel.application.port.inbound.CreatePublishableKeyUseCase;
import com.saleschannel.application.port.outbound.PublishableTokenGeneratorPort;
import com.saleschannel.application.port.outbound.PublishableTokenGeneratorPort.GeneratedToken;
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
public class CreatePublishableKeyService implements CreatePublishableKeyUseCase {
    private static final Logger log = Loggers.getLogger(CreatePublishableKeyService.class);
    private final SalesChannelRepository channels;
    private final PublishableKeyRepository keys;
    private final PublishableTokenGeneratorPort tokens;
    private final IdGenerator ids;
    private final Clock clock;
    private final PublishableKeyManagementPolicy managementPolicy;

    @Override
    public CreatePublishableKeyResult execute(CreatePublishableKeyCommand command) {
        Id channelId = command.salesChannelId();
        SalesChannel channel = channels.findById(channelId)
                .orElseThrow(() -> channelNotFound(channelId));
        ChannelOwner owner = channel.getOwner();
        Id owningMerchantId = channel.getMerchantId();
        PublishableKeyManagementAccess access = command.access();
        managementPolicy.requireAllowed(channelId, owner, owningMerchantId, access);
        GeneratedToken token = tokens.generate();
        String digest = token.digest();
        if (keys.existsByDigest(digest)) {
            String channelValue = channelId.getValue();
            log.warn("Publishable key generation conflict for salesChannelId={}", channelValue);
            SalesChannelServiceError error = new SalesChannelServiceError.PublishableKeyDigestConflict();
            throw new SalesChannelServiceException(error, "Publishable key generation conflict");
        }
        Id keyId = ids.generateId();
        Instant now = clock.instant();
        String label = command.label();
        String displayPrefix = token.displayPrefix();
        PublishableKey key = PublishableKey.create(
                keyId, channelId, label, digest, displayPrefix, now);
        PublishableKey saved = keys.save(key);
        Id savedId = saved.getId();
        Id savedChannelId = saved.getSalesChannelId();
        String savedLabel = saved.getLabel();
        String rawToken = token.rawToken();
        String savedPrefix = saved.getDisplayPrefix();
        Instant createdAt = saved.getCreatedAt();
        String keyValue = savedId.getValue();
        String channelValue = savedChannelId.getValue();
        log.info("Created publishable key keyId={}, salesChannelId={}", keyValue, channelValue);
        return new CreatePublishableKeyResult(savedId, savedChannelId, savedLabel, rawToken, savedPrefix, createdAt);
    }

    private SalesChannelServiceException channelNotFound(Id channelId) {
        String value = channelId.getValue();
        SalesChannelServiceError error = new SalesChannelServiceError.ChannelNotFound(value);
        return new SalesChannelServiceException(error, "Sales channel not found");
    }
}
