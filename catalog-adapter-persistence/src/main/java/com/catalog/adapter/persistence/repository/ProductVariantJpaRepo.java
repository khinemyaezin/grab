package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantJpaRepo extends EntityRepository<ProductVariantEntity, Long>, JpaRepository<ProductVariantEntity, Long>, JpaSpecificationExecutor<ProductVariantEntity> {
    Optional<ProductVariantEntity> findByUuid(String uuid);

    List<ProductVariantEntity> findByProduct_Uuid(String productUuid);

    @Query("""
            SELECT v FROM ProductVariantEntity v
            JOIN FETCH v.product p
            WHERE v.uuid = :variantUuid
            AND p.status = com.catalog.domain.valueobject.ProductStatus.ACTIVE
            AND EXISTS (
                SELECT 1 FROM ProductPublicationEntity pub
                WHERE pub.variantId = v.id AND pub.salesChannelId = :salesChannelId
            )
            """)
    Optional<ProductVariantEntity> findPublishedVariant(
            @Param("variantUuid") String variantUuid,
            @Param("salesChannelId") String salesChannelId);
}
