package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductJpaRepo extends EntityRepository<ProductEntity, Long>, JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {
    Optional<ProductEntity> findByUuid(@Param("uuid") String uuid);

    Optional<ProductEntity> findBySlug(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.uuid = :uuid")
    boolean existsById(@Param("uuid") String uuid);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.slug = :slug AND (:excludeUuid IS NULL OR p.uuid <> :excludeUuid)")
    boolean isSlugTaken(@Param("slug") String slug, @Param("excludeUuid") String excludeUuid);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.slug, LENGTH(:baseSlug) + 2) AS INTEGER)), 0) FROM ProductEntity p WHERE p.slug LIKE CONCAT(:baseSlug, '-%')")
    Optional<Integer> findMaxSlugSuffix(@Param("baseSlug") String baseSlug);
}
