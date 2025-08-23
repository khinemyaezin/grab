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
@Table(name = "costbased_pricing")
@DiscriminatorValue("COST_BASED")
public class CostBasedPricingEntity extends PricingStrategyEntity {
    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "markup_percentage")
    private BigDecimal markupPercentage;
}
