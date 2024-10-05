package com.coolstuff.ecommerce.grab.infrastructure.product.entity.product;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "product_variant_option")
@Getter
@Setter
public class ProductVariantOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariantEntity productVariantEntity;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "variant_type_id")
    private VariantTypeEntity variantTypeEntity;

    @Column(name = "variant_type_value")
    private String variantTypeValue;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "variant_option_id")
    private VariantOptionEntity variantOptionEntity;

    @Column(name = "variant_option_value")
    private String variantOptionValue;

    @ManyToOne
    @JoinColumn(name = "variant_option_unit_id")
    private VariantOptionUnitEntity variantOptionUnitEntity;
}
