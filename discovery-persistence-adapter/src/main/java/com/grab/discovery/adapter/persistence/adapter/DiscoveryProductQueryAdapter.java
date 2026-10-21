package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapper;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductJpaRepository;
import com.grab.discovery.adapter.persistence.specification.jpa.DiscoveryProductSearchSpecification;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

@RequiredArgsConstructor
public class DiscoveryProductQueryAdapter implements DiscoveryProductQueryPort {

    private static final String RESOURCE = "DiscoveryProductEntity";
    private static final String ACTIVE = "ACTIVE";

    private final DiscoveryProductJpaRepository products;
    private final DiscoveryProductSearchSpecification searchSpecification;
    private final DiscoveryProductEntityMapper mapper;
    private final PersistenceExecutor executor;

    @Override
    public Page<DiscoveryProductView> searchVisible(Id salesChannelId, Pageable pageable) {
        String channelIdValue = salesChannelId != null ? salesChannelId.getValue() : null;
        Specification<DiscoveryProductEntity> spec = searchSpecification.visibleForSalesChannel(channelIdValue);
        Page<DiscoveryProductEntity> entityPage = executor.query(RESOURCE, () -> products.findAll(spec, pageable));
        return entityPage.map(mapper::toView);
    }

    @Override
    public Optional<DiscoveryProductView> findVisibleBySlug(Id salesChannelId, String slug) {
        String channelIdValue = salesChannelId != null ? salesChannelId.getValue() : null;
        Optional<DiscoveryProductEntity> entityOpt = executor.query(
                RESOURCE,
                () -> products.findBySalesChannelIdAndSlugAndPublishedTrueAndChannelEnabledTrueAndProductStatus(
                        channelIdValue,
                        slug,
                        ACTIVE
                )
        );
        return entityOpt.map(mapper::toView);
    }
}
