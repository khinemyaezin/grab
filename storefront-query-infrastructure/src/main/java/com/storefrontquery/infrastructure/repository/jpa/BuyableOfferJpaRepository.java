package com.storefrontquery.infrastructure.repository.jpa;

import com.storefrontquery.infrastructure.entity.BuyableOfferEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuyableOfferJpaRepository extends JpaRepository<BuyableOfferEntity, Long> {
    Optional<BuyableOfferEntity> findBySalesChannelIdAndVariantId(String salesChannelId, String variantId);

    Optional<BuyableOfferEntity> findBySalesChannelIdAndSlugAndBuyableTrue(String salesChannelId, String slug);

    Page<BuyableOfferEntity> findBySalesChannelIdAndBuyableTrue(String salesChannelId, Pageable pageable);

    List<BuyableOfferEntity> findByVariantId(String variantId);

    List<BuyableOfferEntity> findByProductId(String productId);

    List<BuyableOfferEntity> findBySku(String sku);

    List<BuyableOfferEntity> findBySalesChannelId(String salesChannelId);
}
