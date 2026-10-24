package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapper;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductJpaRepository;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductVariantJpaRepository;
import com.grab.discovery.adapter.persistence.specification.jpa.DiscoveryProductSearchSpecification;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

@RequiredArgsConstructor
public class DiscoveryProductQueryAdapter implements DiscoveryProductQueryPort {

    private static final String RESOURCE = "DiscoveryProductEntity";
    private static final String ACTIVE = "ACTIVE";

    private final DiscoveryProductJpaRepository products;
    private final DiscoveryProductVariantJpaRepository variants;
    private final DiscoveryProductSearchSpecification searchSpecification;
    private final DiscoveryProductEntityMapper mapper;
    private final PersistenceExecutor executor;

    @Override
    public Page<DiscoveryProductView> searchVisible(
            Id salesChannelId,
            String keyword,
            Id categoryId,
            Pageable pageable
    ) {
        String channelIdValue = salesChannelId != null ? salesChannelId.getValue() : null;
        String categoryIdValue = categoryId != null ? categoryId.getValue() : null;
        Specification<DiscoveryProductEntity> spec = searchSpecification.visibleForSalesChannel(
                channelIdValue,
                keyword,
                categoryIdValue
        );
        Pageable orderedPageable = pageable.isPaged()
                ? PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort().and(Sort.by("productId").ascending())
                )
                : Pageable.unpaged(pageable.getSort().and(Sort.by("productId").ascending()));
        Page<DiscoveryProductEntity> entityPage = executor.query(
                RESOURCE,
                () -> products.findAll(spec, orderedPageable)
        );
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
        return entityOpt.filter(entity -> executor.query(
                        RESOURCE,
                        () -> variants.existsByDiscoveryProduct_IdAndVariantStatus(entity.getId(), ACTIVE)
                ))
                .map(mapper::toView);
    }
}
