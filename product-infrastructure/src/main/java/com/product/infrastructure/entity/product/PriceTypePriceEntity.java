package com.product.infrastructure.entity.product;


import jakarta.persistence.*;

@Entity
@Table(name = "price_type_price")
@IdClass(PriceTypePriceId.class)
public class PriceTypePriceEntity {
    @Id
    private Long priceTypeId;

    @Id
    private Long pricePriceType;

    @ManyToOne
    @JoinColumn(name = "price_type_id", insertable = false, updatable = false)
    private PriceTypeEntity priceTypeEntity;

    @ManyToOne
    @JoinColumn(name = "price_price_type", insertable = false, updatable = false)
    private PriceEntity priceEntity;

    // getters and setters
}
