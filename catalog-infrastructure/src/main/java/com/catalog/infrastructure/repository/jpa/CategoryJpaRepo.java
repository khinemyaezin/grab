package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryJpaRepo extends EntityRepository<CategoryEntity, Long>, JpaRepository<CategoryEntity,Long>{
    Optional<CategoryEntity> findByUuid(String uuid);

    @Query("""
            select ancestor.uuid
            from CategoryEntity node, CategoryEntity ancestor
            where node.uuid = :categoryUuid
              and ancestor.lft <= node.lft
              and ancestor.rgt >= node.rgt
            order by ancestor.depth desc
            """)
    List<String> findAncestorUuidsFromLeaf(@Param("categoryUuid") String categoryUuid);
}
