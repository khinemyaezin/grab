package com.product.infrastructure.entity.product;

import jakarta.persistence.*;


@Entity
@Table(name = "product_price")
@IdClass(ProductPriceId.class)
public class ProductPriceEntity {
    @Id
    private Long productId;

    @Id
    private Long priceProductId;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private ProductEntity productEntity;

    @ManyToOne
    @JoinColumn(name = "price_product_id", insertable = false, updatable = false)
    private PriceEntity priceEntity;

    // getters and setters
}
