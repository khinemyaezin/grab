package com.product.infrastructure.entity.product.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "variant_option")
public class VariantOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "code", unique = true)
    private String code;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_type_id",nullable = false)
    private VariantTypeEntity variantType;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VariantOptionEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(uuid, that.uuid) && Objects.equals(variantType, that.variantType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, variantType);
    }
}
