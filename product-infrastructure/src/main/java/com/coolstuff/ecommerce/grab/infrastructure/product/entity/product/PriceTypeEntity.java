package com.coolstuff.ecommerce.grab.infrastructure.product.entity.product;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;

@Entity
@Table(name = "price_type")
public class PriceTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "priceTypeEntity")
    private Set<PriceEntity> priceEntities;

    // getters and setters
}
