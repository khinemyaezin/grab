package com.pricing.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@Entity
@Table(name = "price_adjustments")
public class PriceAdjustmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "rate")
    private BigDecimal rate; // Tax or discount rate

    @Enumerated(EnumType.STRING)
    private AdjustmentType adjustmentType; // TAX, DISCOUNT

    @ManyToOne
    @JoinColumn(name = "pricing_id")
    private PricingEntity pricing;
}

