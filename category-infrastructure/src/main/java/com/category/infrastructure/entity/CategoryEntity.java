package com.category.infrastructure.entity;

import com.category.domain.aggregate.ICategory;
import com.nestedset.library.annotation.DepthColumn;
import com.nestedset.library.annotation.LeftColumn;
import com.nestedset.library.annotation.NameColumn;
import com.nestedset.library.annotation.RightColumn;
import com.nestedset.library.model.NestedSet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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