package com.pricing.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pricing_strategy")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "pricing_type")
public abstract class PricingStrategyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "pricing_id")
    private PricingEntity pricing;
}