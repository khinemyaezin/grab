package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.ProductPublicationEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductPublicationJpaRepository
        extends JpaRepository<ProductPublicationEntity, ProductPublicationEntityId> {

    Optional<ProductPublicationEntity> findByVariantIdAndSalesChannelId(Long variantId, String salesChannelId);

    List<ProductPublicationEntity> findByVariantId(Long variantId);

    boolean existsByVariantIdAndSalesChannelId(Long variantId, String salesChannelId);

    @Query("""
            SELECT publication FROM ProductPublicationEntity publication, ProductVariantEntity variant
            WHERE publication.variantId = variant.id
              AND variant.product.id = :productId
            """)
    List<ProductPublicationEntity> findByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT v.uuid, pub.salesChannelId FROM ProductPublicationEntity pub
            JOIN ProductVariantEntity v ON v.id = pub.variantId
            """)
    List<Object[]> findAllPublicationVariantUuids();
}
