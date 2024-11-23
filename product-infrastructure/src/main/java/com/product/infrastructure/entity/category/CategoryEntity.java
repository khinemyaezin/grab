package com.product.infrastructure.entity.category;

import com.nestedset.library.annotation.*;
import com.nestedset.library.model.NestedSet;
import com.product.domain.entity.category.ICategory;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "category", indexes = {@Index(name = "leftIndex", columnList = "lft")})
public class CategoryEntity implements NestedSet<Long>, ICategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
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
