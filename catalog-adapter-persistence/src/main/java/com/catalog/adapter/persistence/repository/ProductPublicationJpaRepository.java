package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.ProductPublicationEntityId;
import com.catalog.application.model.read.PublishedProductChannel;
import com.catalog.application.model.read.PublishedVariantChannelView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

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
            SELECT new com.catalog.application.model.read.PublishedVariantChannelView(v.uuid, pub.salesChannelId)
            FROM ProductPublicationEntity pub
            JOIN ProductVariantEntity v ON v.id = pub.variantId
            """)
    List<PublishedVariantChannelView> findAllPublicationVariantUuids();

    @Query("""
            SELECT DISTINCT new com.catalog.application.model.read.PublishedProductChannel(product.uuid, publication.salesChannelId)
            FROM ProductPublicationEntity publication
            JOIN ProductVariantEntity variant ON publication.variantId = variant.id
            JOIN variant.product product
            WHERE (:afterProductId IS NULL
                   OR product.uuid > :afterProductId
                   OR (product.uuid = :afterProductId AND publication.salesChannelId > :afterSalesChannelId))
            ORDER BY product.uuid, publication.salesChannelId
            """)
    List<PublishedProductChannel> findPublishedProductChannelsAfter(
            @Param("afterProductId") String afterProductId,
            @Param("afterSalesChannelId") String afterSalesChannelId,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT new com.catalog.application.model.read.PublishedProductChannel(product.uuid, publication.salesChannelId)
            FROM ProductPublicationEntity publication
            JOIN ProductVariantEntity variant ON publication.variantId = variant.id
            JOIN variant.product product
            WHERE variant.sku = :sku
            ORDER BY product.uuid, publication.salesChannelId
            """)
    List<PublishedProductChannel> findPublishedProductChannelsBySku(
            @Param("sku") String sku
    );

    @Query("""
            SELECT DISTINCT new com.catalog.application.model.read.PublishedProductChannel(product.uuid, publication.salesChannelId)
            FROM ProductPublicationEntity publication
            JOIN ProductVariantEntity variant ON publication.variantId = variant.id
            JOIN variant.product product
            WHERE publication.salesChannelId = :salesChannelId
              AND (:afterProductId IS NULL OR product.uuid > :afterProductId)
            ORDER BY product.uuid
            """)
    List<PublishedProductChannel> findPublishedProductChannelsBySalesChannelAfter(
            @Param("salesChannelId") String salesChannelId,
            @Param("afterProductId") String afterProductId,
            Pageable pageable
    );
}
