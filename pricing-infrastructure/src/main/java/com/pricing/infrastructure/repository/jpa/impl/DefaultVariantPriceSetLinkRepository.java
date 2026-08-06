package com.pricing.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.pricing.infrastructure.entity.VariantPriceSetLinkEntity;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkJpaRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class DefaultVariantPriceSetLinkRepository implements VariantPriceSetLinkRepository {

    private final VariantPriceSetLinkJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public void save(VariantPriceSetLinkView link) {
        executor.command("VariantPriceSetLink", () -> {
            Instant now = Instant.now();
            VariantPriceSetLinkEntity entity = jpaRepository.findById(link.variantId())
                    .orElseGet(VariantPriceSetLinkEntity::new);
            boolean isNew = entity.getVariantId() == null;
            entity.setVariantId(link.variantId());
            entity.setPriceSetId(link.priceSetId());
            entity.setProductId(link.productId());
            entity.setSku(link.sku());
            entity.setMerchantId(link.merchantId());
            if (isNew) {
                Instant createdAt = link.createdAt() == null ? now : link.createdAt();
                entity.setCreatedAt(createdAt);
            }
            Instant updatedAt = link.updatedAt() == null ? now : link.updatedAt();
            entity.setUpdatedAt(updatedAt);
            jpaRepository.save(entity);
        });
    }

    @Override
    public void deleteByVariantId(String variantId) {
        executor.command("VariantPriceSetLink", () -> jpaRepository.deleteById(variantId));
    }

    @Override
    public void deleteByPriceSetId(String priceSetId) {
        executor.command("VariantPriceSetLink", () -> jpaRepository.deleteByPriceSetId(priceSetId));
    }
}
