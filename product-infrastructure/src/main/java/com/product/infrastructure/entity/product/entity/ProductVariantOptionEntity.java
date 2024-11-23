package com.product.infrastructure.entity.product.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "product_variant_option")
public class ProductVariantOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariantEntity productVariant;

    @Setter
    @ManyToOne
    @JoinColumn(name = "variant_type_id")
    private VariantTypeEntity variantType;

    @Column(name = "variant_type_value")
    private String variantTypeValue;

    @Setter
    @ManyToOne
    @JoinColumn(name = "variant_option_id")
    private VariantOptionEntity variantOption;

    @Column(name = "variant_option_value")
    private String variantOptionValue;

    /*@Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_option_unit_id")
    private VariantOptionUnitEntity variantOptionUnit;*/


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariantOptionEntity that = (ProductVariantOptionEntity) o;
        return Objects.equals(variantTypeValue, that.variantTypeValue) && Objects.equals(variantOptionValue, that.variantOptionValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantTypeValue, variantOptionValue);
    }
}
