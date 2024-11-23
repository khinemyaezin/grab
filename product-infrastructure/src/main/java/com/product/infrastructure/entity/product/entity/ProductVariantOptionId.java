package com.product.infrastructure.entity.product.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class ProductVariantOptionId implements Serializable {
    private Long variantTypeId;
    private Long variantOptionId;
}
