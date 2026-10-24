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
}
