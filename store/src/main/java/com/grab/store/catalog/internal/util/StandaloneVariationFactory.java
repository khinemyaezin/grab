package com.grab.store.catalog.internal.util;

import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.IdGenerator;

import java.util.List;
import java.util.Objects;

public final class StandaloneVariationFactory {
    public static final String TYPE_ID = "system:type:title";
    public static final String OPTION_ID = "system:option:default-title";

    public static List<ProductVariation> create(IdGenerator idGenerator) {
        return List.of(new ProductVariation(
                idGenerator.convertIdFrom(OPTION_ID),
                idGenerator.convertIdFrom(TYPE_ID)
        ));
    }

    public static boolean isStandAloneVariation(ProductVariation variation) {
        if( variation == null
                || variation.getOptionId() == null
                || variation.getTypeId() == null)
            return false;

        return Objects.equals(variation.getOptionId().getValue(), OPTION_ID)
                && Objects.equals(variation.getTypeId().getValue(), TYPE_ID);
    }
}
