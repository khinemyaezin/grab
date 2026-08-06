package com.pricing.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.pricing.infrastructure.entity.VariantPriceSetLinkEntity;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkJpaRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkQueryRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class DefaultVariantPriceSetLinkQueryRepository implements VariantPriceSetLinkQueryRepository {

    private final VariantPriceSetLinkJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public List<VariantPriceSetLinkView> findByVariantIds(Collection<String> variantIds) {
        return executor.query("VariantPriceSetLink", () ->
                jpaRepository.findByVariantIdIn(variantIds).stream()
                        .map(this::toView)
                        .toList());
    }

    private VariantPriceSetLinkView toView(VariantPriceSetLinkEntity entity) {
        return new VariantPriceSetLinkView(
                entity.getVariantId(),
                entity.getPriceSetId(),
                entity.getProductId(),
                entity.getSku(),
                entity.getMerchantId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
