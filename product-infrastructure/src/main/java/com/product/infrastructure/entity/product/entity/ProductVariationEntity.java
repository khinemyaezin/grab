package com.product.infrastructure.entity.product.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_variant_option",
        uniqueConstraints = @UniqueConstraint(
        name = "uk_variant_option_value",
        columnNames = {"variant_option_id", "variant_option_value"}
))
public class ProductVariationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariantEntity productVariant;
    
    @ManyToOne
    @JoinColumn(name = "variant_type_id")
    private VariantTypeEntity variantType;
    
    @ManyToOne
    @JoinColumn(name = "variant_option_id")
    private VariantOptionEntity variantOption;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProductVariationEntity that)) return false;
        return Objects.equals(variantOption, that.variantOption) && Objects.equals(variantType, that.variantType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantOption, variantType);
    }
}
