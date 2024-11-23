package com.product.infrastructure.repository.product;

import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductEntityRepository extends EntityRepository<ProductEntity, Long>, JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByUuid(@Param("uuid") String uuid);
}
