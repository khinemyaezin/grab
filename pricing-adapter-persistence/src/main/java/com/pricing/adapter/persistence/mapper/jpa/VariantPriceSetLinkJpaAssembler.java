package com.pricing.adapter.persistence.mapper.jpa;

import com.pricing.adapter.persistence.entity.VariantPriceSetLinkEntity;
import com.pricing.application.model.read.VariantPriceSetLinkView;

public interface VariantPriceSetLinkJpaAssembler {
    VariantPriceSetLinkEntity buildEntity(VariantPriceSetLinkView view, VariantPriceSetLinkEntity entity);
    VariantPriceSetLinkView toView(VariantPriceSetLinkEntity entity);
}
