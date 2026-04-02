package com.grab.store.catalog.internal.util;

import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import lombok.AllArgsConstructor;

import java.util.List;

public final class StandaloneVariantDefaults {

    public static final String TYPE_NAME = "Title";
    public static final String OPTION_NAME = "Default Title";
    public static final String TYPE_ID = "system:type:title";
    public static final String OPTION_ID = "system:option:default-title";

    public static List<ProductVariation> defaultVariations(IdGenerator idGenerator) {
        return List.of(new ProductVariation(
                idGenerator.convertIdFrom(OPTION_ID),
                idGenerator.convertIdFrom(TYPE_ID)
        ));
    }
}
