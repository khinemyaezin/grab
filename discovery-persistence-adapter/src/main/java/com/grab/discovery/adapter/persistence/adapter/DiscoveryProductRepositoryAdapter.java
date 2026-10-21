package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapper;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductJpaRepository;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DiscoveryProductRepositoryAdapter implements DiscoveryProductRepository {

    private static final String RESOURCE = "DiscoveryProductEntity";

    private final DiscoveryProductJpaRepository products;
    private final DiscoveryProductEntityMapper mapper;
    private final PersistenceExecutor executor;

    @Override
    public Optional<DiscoveryProductProjection> findBySalesChannelAndProduct(Id channelId, Id productId) {
        String channelIdValue = value(channelId);
        String productIdValue = value(productId);
        Optional<DiscoveryProductEntity> entityOpt = executor.query(
                RESOURCE,
                () -> products.findBySalesChannelIdAndProductId(channelIdValue, productIdValue)
        );
        return entityOpt.map(mapper::toProjection);
    }

    @Override
    public List<DiscoveryProductProjection> findByVariant(Id variantId) {
        String variantIdValue = value(variantId);
        List<DiscoveryProductEntity> entities = executor.query(
                RESOURCE,
                () -> products.findByVariantIdInVariants(variantIdValue)
        );
        List<DiscoveryProductProjection> projections = new ArrayList<>();
        for (DiscoveryProductEntity entity : entities) {
            projections.add(mapper.toProjection(entity));
        }
        return projections;
    }

    @Override
    public List<DiscoveryProductProjection> findByProduct(Id productId) {
        String productIdValue = value(productId);
        List<DiscoveryProductEntity> entities = executor.query(
                RESOURCE,
                () -> products.findByProductId(productIdValue)
        );
        List<DiscoveryProductProjection> projections = new ArrayList<>();
        for (DiscoveryProductEntity entity : entities) {
            projections.add(mapper.toProjection(entity));
        }
        return projections;
    }

    @Override
    public List<DiscoveryProductProjection> findBySku(String sku) {
        List<DiscoveryProductEntity> entities = executor.query(
                RESOURCE,
                () -> products.findBySkuInVariants(sku)
        );
        List<DiscoveryProductProjection> projections = new ArrayList<>();
        for (DiscoveryProductEntity entity : entities) {
            projections.add(mapper.toProjection(entity));
        }
        return projections;
    }

    @Override
    public List<DiscoveryProductProjection> findBySalesChannel(Id salesChannelId) {
        String salesChannelIdValue = value(salesChannelId);
        List<DiscoveryProductEntity> entities = executor.query(
                RESOURCE,
                () -> products.findBySalesChannelId(salesChannelIdValue)
        );
        List<DiscoveryProductProjection> projections = new ArrayList<>();
        for (DiscoveryProductEntity entity : entities) {
            projections.add(mapper.toProjection(entity));
        }
        return projections;
    }

    @Override
    public void save(DiscoveryProductProjection projection) {
        String channelIdValue = value(projection.salesChannelId());
        String productIdValue = value(projection.productId());

        executor.command(RESOURCE, () -> {
            Optional<DiscoveryProductEntity> entityOpt =
                    products.findBySalesChannelIdAndProductId(channelIdValue, productIdValue);
            DiscoveryProductEntity entity = entityOpt.orElseGet(DiscoveryProductEntity::new);
            mapper.updateEntity(projection, entity);
            products.save(entity);
        });
    }

    private String value(Id id) {
        if (id == null) {
            return null;
        }
        return id.getValue();
    }
}
