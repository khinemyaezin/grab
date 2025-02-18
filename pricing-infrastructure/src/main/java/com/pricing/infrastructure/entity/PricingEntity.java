package com.pricing.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "pricing")
public class PricingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product", unique = true)
    private String product;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "default-currency", nullable = false)
    private String defaultCurrency;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "pricing", orphanRemoval = true)
    private List<PricingStrategyEntity> pricingStrategies = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "pricing", orphanRemoval = true)
    private List<PriceAdjustmentEntity> priceAdjustments = new ArrayList<>();

    public void addStrategy(PricingStrategyEntity pricingStrategy) {
        pricingStrategy.setPricing(this);
        pricingStrategies.add(pricingStrategy);
    }

    public void addAdjustment(PriceAdjustmentEntity priceAdjustment) {
        priceAdjustment.setPricing(this);
        priceAdjustments.add(priceAdjustment);
    }

    public void removeStrategy(PricingStrategyEntity entity){
        pricingStrategies.remove(entity);
    }

    public void removeAdjustment(PriceAdjustmentEntity entity){
        priceAdjustments.remove(entity);
    }
}
