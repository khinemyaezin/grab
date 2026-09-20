package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.catalog.adapter.persistence.repository.EntityRepository;
import com.catalog.application.readmodel.CategoryView;
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
            select new com.catalog.application.readmodel.CategoryView(
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

    List<CategoryEntity> findByDepthOrderByLftAsc(Integer depth);
}
