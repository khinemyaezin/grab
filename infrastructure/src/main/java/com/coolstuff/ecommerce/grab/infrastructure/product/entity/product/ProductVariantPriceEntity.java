package com.coolstuff.ecommerce.grab.infrastructure.product.entity.product;

import jakarta.persistence.*;


@Entity
@Table(name = "product_variant_price")
@IdClass(ProductVariantPriceId.class)
public class ProductVariantPriceEntity {
    @Id
    private Long productVariantId;

    @Id
    private Long priceProductVariantId;

    @ManyToOne
    @JoinColumn(name = "product_variant_id", insertable = false, updatable = false)
    private ProductVariantEntity productVariantEntity;

    @ManyToOne
    @JoinColumn(name = "price_product_variant_id", insertable = false, updatable = false)
    private PriceEntity priceEntity;

    // getters and setters
}
