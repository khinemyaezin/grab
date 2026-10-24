package com.inventory.adapter.persistence.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.adapter.persistence.entity.ProductVariantViewEntity;
import com.inventory.adapter.persistence.mapper.ProductVariantProjectionMapper;
import com.inventory.adapter.persistence.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.application.model.write.ProductVariantProjection;
import com.inventory.application.port.outbound.ProductVariantProjectionRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProductVariantProjectionRepositoryAdapter implements ProductVariantProjectionRepository {
    private static final String RESOURCE = "ProductVariantView";
    private final ProductVariantViewJpaRepository repository;
    private final ProductVariantProjectionMapper mapper;
    private final PersistenceExecutor executor;

    @Override
    public Optional<ProductVariantProjection> findByVariant(Id variantId) {
        String variantValue = variantId.getValue();
        Optional<ProductVariantViewEntity> entity = executor.query(
                RESOURCE, () -> repository.findByVariantUuid(variantValue)
        );
        return entity.map(mapper::toProjection);
    }

    @Override
    public List<ProductVariantProjection> findByProduct(Id productId) {
        String productValue = productId.getValue();
        List<ProductVariantViewEntity> entities = executor.query(
                RESOURCE, () -> repository.findAllByProductUuid(productValue)
        );
        return entities.stream().map(mapper::toProjection).toList();
    }

    @Override
    public void save(ProductVariantProjection projection) {
        String variantValue = projection.variantId().getValue();
        executor.command(RESOURCE, () -> {
            ProductVariantViewEntity entity = repository.findByVariantUuid(variantValue)
                    .orElseGet(ProductVariantViewEntity::new);
            mapper.updateEntity(projection, entity);
            repository.saveAndFlush(entity);
        });
    }
}
