package com.coolstuff.ecommerce.grab.domain.product.entity.product;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.ReadableCategory;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ReadableProductVariant;

import java.util.Set;

@Getter
@Setter
public class ReadableProduct {
    private String uuid;
    private String name;
    private ReadableCategory category;
    private Set<ReadableProductVariant> readableProductVariants;
}
