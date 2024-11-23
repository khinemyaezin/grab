package com.product.domain.factory.product;

import lombok.Builder;

@Builder
public class ProductSpec {
    private String uuid;
    private String name;
    private String categoryId;

}
