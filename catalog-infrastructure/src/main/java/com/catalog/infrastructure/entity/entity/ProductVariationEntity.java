package com.catalog.infrastructure.entity.entity;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_variant_option")
public class ProductVariationEntity {

    @EmbeddedId
    private ProductVariationId id;

    @MapsId("variantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariantEntity productVariant;

    @Column(name = "variant_option_value")
    private String variantOptionValue;

    @Column(name = "variant_type_value")
    private String variantTypeValue;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariationId implements Serializable {
        @Column(name = "variant_option_id")
        private String variantOptionUuid;

        @Column(name = "variant_type_id")
        private String variantTypeUuid;

        private Long variantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariationEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
