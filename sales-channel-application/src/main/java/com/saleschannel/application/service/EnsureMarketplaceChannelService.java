package com.saleschannel.application.service;

import com.grab.framework.id.IdGenerator;
import com.saleschannel.application.model.write.EnsureMarketplaceChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import com.saleschannel.application.port.inbound.EnsureMarketplaceChannelUseCase;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class EnsureMarketplaceChannelService implements EnsureMarketplaceChannelUseCase {
    private static final String MARKETPLACE_NAME = "Default Sale Channel";

    private final SalesChannelRepository salesChannels;
    private final IdGenerator idGenerator;

    @Override
    public ProvisionSalesChannelResult execute(EnsureMarketplaceChannelCommand command) {
        return salesChannels.findMarketplace()
                .map(this::existingChannel)
                .orElseGet(this::createMarketplace);
    }

    private ProvisionSalesChannelResult existingChannel(SalesChannel channel) {
        return new ProvisionSalesChannelResult(channel.getId(), false);
    }

    private ProvisionSalesChannelResult createMarketplace() {
        Instant now = Instant.now();
        SalesChannel channel = SalesChannel.createMarketplace(idGenerator.generateId(), MARKETPLACE_NAME, now);
        SalesChannel savedChannel = salesChannels.save(channel);
        return new ProvisionSalesChannelResult(savedChannel.getId(), true);
    }
}
