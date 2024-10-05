package com.coolstuff.ecommerce.grab.infrastructure.product.entity.product;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "variant_option_unit")
public class VariantOptionUnitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    private String name;

    @ManyToOne
    @JoinColumn(name = "variant_type_id")
    private VariantTypeEntity variantTypeEntity;

    // getters and setters
}
