package com.product.infrastructure.entity.product;

import com.product.infrastructure.entity.category.CategoryEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product")
@Getter
@Setter
public class ProductEntity implements Serializable {
    @Id
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;

    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity categoryEntity;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "media_id")
    private MediaEntity mediaEntity;

    @OneToMany(mappedBy = "productEntity",fetch = FetchType.EAGER)
    private Set<ProductVariantEntity> productVariantEntities = new HashSet<>();
}
