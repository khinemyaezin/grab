package com.product.infrastructure.entity.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "variant_type")
public class VariantTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @OneToMany(mappedBy = "variantType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<VariantOptionEntity> variantOptions;

    @OneToMany(mappedBy = "variantType", fetch = FetchType.LAZY)
    private Set<VariantOptionUnitEntity> variantOptionUnits;
}