package com.pricing.adapter.persistence.mapper.jpa.impl;

import com.pricing.adapter.persistence.entity.VariantPriceSetLinkEntity;
import com.pricing.adapter.persistence.mapper.jpa.VariantPriceSetLinkJpaAssembler;
import com.pricing.application.model.read.VariantPriceSetLinkView;

import java.time.Instant;

public class VariantPriceSetLinkJpaAssemblerImpl implements VariantPriceSetLinkJpaAssembler {

    @Override
    public VariantPriceSetLinkEntity buildEntity(VariantPriceSetLinkView view, VariantPriceSetLinkEntity entity) {
        VariantPriceSetLinkEntity target = entity == null ? new VariantPriceSetLinkEntity() : entity;
        boolean isNew = target.getVariantId() == null;
        
        target.setVariantId(view.variantId());
        target.setPriceSetId(view.priceSetId());
        target.setProductId(view.productId());
        target.setSku(view.sku());
        target.setMerchantId(view.merchantId());
        
        Instant now = Instant.now();
        if (isNew) {
            Instant createdAt = view.createdAt() == null ? now : view.createdAt();
            target.setCreatedAt(createdAt);
        }
        Instant updatedAt = view.updatedAt() == null ? now : view.updatedAt();
        target.setUpdatedAt(updatedAt);
        
        return target;
    }

    @Override
    public VariantPriceSetLinkView toView(VariantPriceSetLinkEntity entity) {
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
