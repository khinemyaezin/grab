package com.product.infrastructure.entity.product;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "price")
public class PriceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity productEntity;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariantEntity productVariantEntity;

    @ManyToOne
    @JoinColumn(name = "price_type")
    private PriceTypeEntity priceTypeEntity;

    private double amount;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    // getters and setters
}
