package com.pricing.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "dynamic_pricing")
@DiscriminatorValue("DYNAMIC")
public class DynamicPricingEntity extends PricingStrategyEntity {

    @Lob
    @Column(name = "pricing_rules")
    private String pricingRules; // JSON representation of rules

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

}