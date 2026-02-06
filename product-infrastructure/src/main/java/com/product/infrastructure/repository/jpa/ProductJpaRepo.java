package com.product.infrastructure.repository.jpa;

import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductJpaRepo extends EntityRepository<ProductEntity, Long>, JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByUuid(@Param("uuid") String uuid);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.uuid = :uuid")
    boolean existsById(@Param("uuid") String uuid);
}
