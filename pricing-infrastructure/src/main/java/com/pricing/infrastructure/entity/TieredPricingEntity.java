package com.pricing.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "tiered_pricing")
@DiscriminatorValue("TIERED")
public class TieredPricingEntity extends PricingStrategyEntity {

    @ElementCollection
    @CollectionTable(name = "joined_tiered_pricing", joinColumns = @JoinColumn(name = "pricing_id"))
    @MapKeyColumn(name = "quantity_threshold")
    @Column(name = "price")
    private Map<Integer, BigDecimal> tierPrices;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;
}