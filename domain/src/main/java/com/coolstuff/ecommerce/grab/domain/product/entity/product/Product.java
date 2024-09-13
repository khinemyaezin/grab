package com.coolstuff.ecommerce.grab.domain.product.entity.product;

import lombok.Getter;
import lombok.Setter;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.Category;
import com.coolstuff.ecommerce.grab.domain.product.entity.product_variant.ProductVariant;

import java.util.Set;

@Getter
@Setter
public class Product {
    private String id;
    private String uuid;
    private String name;
    private Category category;
    private Set<ProductVariant> productVariants;
}
