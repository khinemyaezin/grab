package com.coolstuff.ecommerce.grab.infrastructure.product.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;

@Entity
@Table(name = "variant_type")
@Getter
@Setter
public class VariantTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @OneToMany(mappedBy = "variantTypeEntity")
    private Set<VariantOptionEntity> variantOptionEntities;

    @OneToMany(mappedBy = "variantTypeEntity")
    private Set<VariantOptionUnitEntity> variantOptionUnitEntities;

    // getters and setters
}