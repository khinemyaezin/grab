package com.coolstuff.ecommerce.grab.infrastructure.product.entity.category;


import com.coolstuff.core.nestedset.columns.DepthColumn;
import com.coolstuff.core.nestedset.columns.LeftColumn;
import com.coolstuff.core.nestedset.columns.NameColumn;
import com.coolstuff.core.nestedset.columns.RightColumn;
import com.coolstuff.core.nestedset.model.NodeComponent;
import com.coolstuff.ecommerce.grab.infrastructure.product.entity.product.FeatureEntity;
import com.coolstuff.ecommerce.grab.infrastructure.product.entity.product.ProductEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "category", indexes = {@Index(name = "leftIndex", columnList = "lft")})
public class CategoryEntity extends NodeComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    @UuidGenerator
    private String uuid;

    @NameColumn
    private String name;

    @LeftColumn
    private Integer lft;

    @RightColumn
    private Integer rgt;

    @DepthColumn
    private Integer depth;

    @OneToMany(mappedBy = "categoryEntity")
    private Set<ProductEntity> productEntities;

    @OneToMany(mappedBy = "categoryEntity")
    private Set<FeatureEntity> featureEntities;

    public CategoryEntity(Long id, String name, Integer lft, Integer rgt, Integer depth) {
        this.id = id;
        this.name = name;
        this.lft = lft;
        this.rgt = rgt;
        this.depth = Math.toIntExact(depth);
    }

}
