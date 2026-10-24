package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.entity.DiscoveryProductVariantEntity;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapper;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductJpaRepository;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductVariantJpaRepository;
import com.grab.discovery.application.model.read.DiscoveryProductKey;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RequiredArgsConstructor
public class DiscoveryProductRepositoryAdapter implements DiscoveryProductRepository {

    private static final String RESOURCE = "DiscoveryProductEntity";

    private final DiscoveryProductJpaRepository products;
    private final DiscoveryProductVariantJpaRepository variants;
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
        List<DiscoveryProductVariantEntity> memberships = executor.query(
                RESOURCE,
                () -> variants.findByVariantId(variantIdValue)
        );
        return projectionsFromMemberships(memberships);
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
        List<DiscoveryProductVariantEntity> memberships = executor.query(
                RESOURCE,
                () -> variants.findBySku(sku)
        );
        return projectionsFromMemberships(memberships);
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
    public List<DiscoveryProductKey> findKeysAfter(Id productId, Id salesChannelId, int limit) {
        return executor.query(RESOURCE, () -> {
            PageRequest page = PageRequest.of(0, limit);
            if (productId == null || salesChannelId == null) {
                return products.findKeys(page);
            }
            return products.findKeysAfter(productId.getValue(), salesChannelId.getValue(), page);
        });
    }

    @Override
    public void save(DiscoveryProductProjection projection) {
        String channelIdValue = value(projection.salesChannelId());
        String productIdValue = value(projection.productId());

        executor.command(RESOURCE, () -> {
            Optional<DiscoveryProductEntity> entityOpt = products.findBySalesChannelIdAndProductId(channelIdValue, productIdValue);
            DiscoveryProductEntity entity = entityOpt.orElseGet(DiscoveryProductEntity::new);
            boolean expectsAbsent = projection.version() == DiscoveryProductProjection.NEW_PROJECTION_VERSION;
            boolean existenceChanged = expectsAbsent ? entityOpt.isPresent() : entityOpt.isEmpty();
            boolean versionChanged = entityOpt.isPresent() && entity.getVersion() != projection.version();
            if (existenceChanged || versionChanged) {
                throw new ObjectOptimisticLockingFailureException(DiscoveryProductEntity.class, entity.getId());
            }
            mapper.updateEntity(projection, entity);
            DiscoveryProductEntity persisted = products.saveAndFlush(entity);
            replaceMemberships(persisted, projection);
        });
    }

    private List<DiscoveryProductProjection> projectionsFromMemberships(
            List<DiscoveryProductVariantEntity> memberships
    ) {
        return memberships.stream()
                .map(DiscoveryProductVariantEntity::getDiscoveryProduct)
                .distinct()
                .map(mapper::toProjection)
                .toList();
    }

    private void replaceMemberships(
            DiscoveryProductEntity entity,
            DiscoveryProductProjection projection
    ) {
        variants.deleteForProduct(entity.getId());
        List<DiscoveryProductVariantEntity> memberships = projection.variants().stream()
                .map(variant -> membership(entity, variant))
                .toList();
        variants.saveAllAndFlush(memberships);
    }

    private DiscoveryProductVariantEntity membership(
            DiscoveryProductEntity entity,
            DiscoveryProductProjection.VariantFact variant
    ) {
        DiscoveryProductVariantEntity membership = new DiscoveryProductVariantEntity();
        membership.setDiscoveryProduct(entity);
        membership.setVariantId(variant.variantId().getValue());
        membership.setSku(variant.sku());
        membership.setVariantStatus(variant.status());
        return membership;
    }

    private String value(Id id) {
        if (id == null) {
            return null;
        }
        return id.getValue();
    }
}
