package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductPublicationJpaRepository
        extends JpaRepository<ProductPublicationEntity, ProductPublicationEntityId> {

    Optional<ProductPublicationEntity> findByProductIdAndSalesChannelId(Long productId, String salesChannelId);

    List<ProductPublicationEntity> findByProductId(Long productId);

    boolean existsByProductIdAndSalesChannelId(Long productId, String salesChannelId);
}
