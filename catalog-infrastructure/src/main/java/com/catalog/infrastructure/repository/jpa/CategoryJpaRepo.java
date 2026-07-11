package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.EntityRepository;
import com.catalog.infrastructure.view.CategoryView;
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

    @Query("""
            select new com.catalog.infrastructure.view.CategoryView(
                c.uuid,
                c.name,
                p.uuid,
                c.active,
                c.listingAllowed,
                c.c2cAllowed
            )
            from CategoryEntity c
            left join CategoryEntity p on p.lft < c.lft and p.rgt > c.rgt and p.depth = c.depth - 1
            where c.uuid in :categoryUuid
            """)
    List<CategoryView> findAllByUuids(@Param("categoryUuid") List<String> categoryUuids);
}
