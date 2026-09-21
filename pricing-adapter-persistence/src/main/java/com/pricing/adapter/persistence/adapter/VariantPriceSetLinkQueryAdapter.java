package com.pricing.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.pricing.adapter.persistence.entity.VariantPriceSetLinkEntity;
import com.pricing.adapter.persistence.repository.jpa.VariantPriceSetLinkJpaRepository;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class VariantPriceSetLinkQueryAdapter implements VariantPriceSetLinkQueryPort {

    private final VariantPriceSetLinkJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public List<VariantPriceSetLinkView> findByVariantIds(Collection<String> variantIds) {
        return executor.query("VariantPriceSetLink", () ->
                jpaRepository.findByVariantIdIn(variantIds).stream()
                        .map(this::toView)
                        .toList());
    }

    @Override
    public List<VariantPriceSetLinkView> findByPriceSetId(String priceSetId) {
        return executor.query("VariantPriceSetLink", () ->
                jpaRepository.findByPriceSetId(priceSetId).stream()
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
