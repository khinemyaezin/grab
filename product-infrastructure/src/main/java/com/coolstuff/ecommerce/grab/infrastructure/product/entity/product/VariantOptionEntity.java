package com.coolstuff.ecommerce.grab.infrastructure.product.entity.product;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "variant_option")
@Getter
@Setter
public class VariantOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;
    @Column(name = "code", unique = true)
    private String code;

    private String name;

    @ManyToOne
    @JoinColumn(name = "variant_type_id")
    private VariantTypeEntity variantTypeEntity;

    // getters and setters
}
