package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "variant_option_unit")
public class VariantOptionUnitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    private String name;

    @ManyToOne
    @JoinColumn(name = "variant_type_id")
    private VariantTypeEntity variantType;
}
