package com.product.domain.entity.product;

import com.product.domain.entity.category.AbstractCategory;
import com.product.domain.entity.product_variant.ProductVariant;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Product {
    private String uuid;
    private String name;
    private AbstractCategory category;
    private Set<ProductVariant> productVariants;
}
