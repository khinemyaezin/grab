package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.repository.ProductPublicationJpaRepository;
import com.catalog.application.model.read.PublishedProductChannel;
import com.catalog.application.port.outbound.PublishedProductChannelQueryPort;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RequiredArgsConstructor
public class PublishedProductChannelQueryAdapter implements PublishedProductChannelQueryPort {

    private static final String RESOURCE = "ProductPublicationEntity";

    private final ProductPublicationJpaRepository publicationRepository;
    private final PersistenceExecutor executor;

    @Override
    public List<PublishedProductChannel> findAfter(Id afterProductId, Id afterSalesChannelId, int limit) {
        String productCursor = afterProductId == null ? null : afterProductId.getValue();
        String channelCursor = afterSalesChannelId == null ? null : afterSalesChannelId.getValue();
        PageRequest page = PageRequest.of(0, limit);
        return executor.query(RESOURCE, () -> publicationRepository.findPublishedProductChannelsAfter(
                productCursor,
                channelCursor,
                page
        ));
    }

    @Override
    public List<PublishedProductChannel> findBySku(String sku) {
        return executor.query(RESOURCE, () -> publicationRepository.findPublishedProductChannelsBySku(sku));
    }

    @Override
    public List<PublishedProductChannel> findBySalesChannelAfter(
            Id salesChannelId,
            Id afterProductId,
            int limit
    ) {
        String salesChannelIdValue = salesChannelId.getValue();
        String productCursor = afterProductId == null ? null : afterProductId.getValue();
        PageRequest page = PageRequest.of(0, limit);
        return executor.query(RESOURCE, () -> publicationRepository.findPublishedProductChannelsBySalesChannelAfter(
                salesChannelIdValue,
                productCursor,
                page
        ));
    }
}
