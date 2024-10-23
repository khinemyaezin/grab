package com.product.infrastructure.entity.category;

import com.nestedset.library.annotation.*;
import com.nestedset.library.model.NestedSet;
import com.product.domain.entity.category.ICategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;


@Getter
@Setter
@Entity
@Table(name = "category", indexes = {@Index(name = "leftIndex", columnList = "lft")})
public class CategoryEntity extends NestedSet implements ICategory {
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
}
