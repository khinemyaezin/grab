package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntityId;
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
}
