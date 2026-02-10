package com.catalog.infrastructure.entity.meta;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CategoryEntity.class)
public class CategoryEntity_ {

    // Define each attribute as volatile for use in Criteria API and MapStruct mappings
    public static volatile SingularAttribute<CategoryEntity, Long> id;
    public static volatile SingularAttribute<CategoryEntity, String> uuid;
    public static volatile SingularAttribute<CategoryEntity, String> name;
    public static volatile SingularAttribute<CategoryEntity, Integer> lft;
    public static volatile SingularAttribute<CategoryEntity, Integer> rgt;
    public static volatile SingularAttribute<CategoryEntity, Integer> depth;

    // Optional: Define constants for attribute names as type-safe strings
    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String LFT = "lft";
    public static final String RGT = "rgt";
    public static final String DEPTH = "depth";
}
