package com.saleschannel.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.domain.repository.SalesChannelRepository;
import com.saleschannel.infrastructure.entity.SalesChannelEntity;
import com.saleschannel.infrastructure.mapper.jpa.impl.SalesChannelJpaAssembler;
import com.saleschannel.infrastructure.repository.jpa.SalesChannelJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultSalesChannelRepository implements SalesChannelRepository {
    private final SalesChannelJpaRepository salesChannels;
    private final SalesChannelJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<SalesChannel> findById(Id id) {
        return executor.query("SalesChannel", () ->
                salesChannels.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public Optional<SalesChannel> findWebsiteByMerchantId(Id merchantId) {
        return executor.query("SalesChannel", () ->
                salesChannels.findByTypeAndMerchantId(ChannelType.WEBSITE, merchantId.getValue())
                        .map(assembler::toDomain));
    }

    @Override
    public Optional<SalesChannel> findMarketplace() {
        return executor.query("SalesChannel", () ->
                salesChannels.findFirstByType(ChannelType.MARKETPLACE).map(assembler::toDomain));
    }

    @Override
    public boolean existsWebsiteByMerchantId(Id merchantId) {
        return executor.query("SalesChannel", () ->
                salesChannels.existsByTypeAndMerchantId(ChannelType.WEBSITE, merchantId.getValue()));
    }

    @Override
    public boolean existsMarketplace() {
        return executor.query("SalesChannel", () -> salesChannels.existsByType(ChannelType.MARKETPLACE));
    }

    @Override
    public SalesChannel save(SalesChannel salesChannel) {
        return executor.command("SalesChannel", () -> {
            SalesChannelEntity existing = salesChannels.findByUuid(salesChannel.getId().getValue()).orElse(null);
            SalesChannelEntity saved = salesChannels.save(assembler.toEntity(salesChannel, existing));
            List<Event> pending = salesChannel.pullEvents();
            events.produce("SalesChannel", salesChannel.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }
}
