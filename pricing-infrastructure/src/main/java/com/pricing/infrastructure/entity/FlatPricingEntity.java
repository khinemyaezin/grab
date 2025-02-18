package com.pricing.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "flat_pricing")
@DiscriminatorValue("FLAT")
public class FlatPricingEntity extends PricingStrategyEntity {
    @Column(name = "flat_price")
    private BigDecimal flatPrice;
}
