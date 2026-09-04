package com.pricing.infrastructure.mapper.jpa;

import com.pricing.infrastructure.entity.VariantPriceSetLinkEntity;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;

public interface VariantPriceSetLinkJpaAssembler {
    VariantPriceSetLinkEntity buildEntity(VariantPriceSetLinkView view, VariantPriceSetLinkEntity entity);
    VariantPriceSetLinkView toView(VariantPriceSetLinkEntity entity);
}
