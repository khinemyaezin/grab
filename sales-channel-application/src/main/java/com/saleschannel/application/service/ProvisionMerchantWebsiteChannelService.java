package com.saleschannel.application.service;

import com.grab.framework.id.IdGenerator;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import com.saleschannel.application.port.inbound.ProvisionMerchantWebsiteChannelUseCase;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor
public class ProvisionMerchantWebsiteChannelService implements ProvisionMerchantWebsiteChannelUseCase {
    private static final String WEBSITE_NAME = "Website";

    private final SalesChannelRepository salesChannels;
    private final IdGenerator idGenerator;

    @Override
    public ProvisionSalesChannelResult execute(ProvisionMerchantWebsiteChannelCommand command) {
        Objects.requireNonNull(command.merchantId(), "merchantId is required");
        return salesChannels.findWebsiteByMerchantId(command.merchantId())
                .map(this::existingChannel)
                .orElseGet(() -> createWebsite(command));
    }

    private ProvisionSalesChannelResult existingChannel(SalesChannel channel) {
        return new ProvisionSalesChannelResult(channel.getId(), false);
    }

    private ProvisionSalesChannelResult createWebsite(ProvisionMerchantWebsiteChannelCommand command) {
        Instant now = Instant.now();
        SalesChannel channel = SalesChannel.createWebsite(
                idGenerator.generateId(),
                command.merchantId(),
                WEBSITE_NAME,
                now
        );
        SalesChannel savedChannel = salesChannels.save(channel);
        return new ProvisionSalesChannelResult(savedChannel.getId(), true);
    }
}
