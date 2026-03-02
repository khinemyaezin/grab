package com.catalog.infrastructure.entity.entity;

import com.nestedset.library.annotation.DepthColumn;
import com.nestedset.library.annotation.LeftColumn;
import com.nestedset.library.annotation.NameColumn;
import com.nestedset.library.annotation.RightColumn;
import com.nestedset.library.model.NestedSet;
import com.catalog.infrastructure.view.ICategory;
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

    @Column
    private Boolean active;

//    public CategoryEntity(Long id, String name, Integer lft, Integer rgt, Integer depth) {
//        this.id = id;
//        this.name = name;
//        this.lft = lft;
//        this.rgt = rgt;
//        this.depth = depth;
//    }

    public CategoryEntity() {

    }
}