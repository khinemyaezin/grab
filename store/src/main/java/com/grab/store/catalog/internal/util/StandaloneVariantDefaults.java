package com.grab.store.catalog.internal.util;

import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.impl.CommonId;

import java.util.List;

public final class StandaloneVariantDefaults {

    public static final String TYPE_NAME = "Title";
    public static final String OPTION_NAME = "Default Title";
    public static final String TYPE_ID = "system:type:title";
    public static final String OPTION_ID = "system:option:default-title";

    private StandaloneVariantDefaults() {
    }

    public static List<ProductVariation> defaultVariations() {
        return List.of(new ProductVariation(
                OPTION_NAME,
                new CommonId(OPTION_ID),
                TYPE_NAME,
                new CommonId(TYPE_ID)
        ));
    }
}
