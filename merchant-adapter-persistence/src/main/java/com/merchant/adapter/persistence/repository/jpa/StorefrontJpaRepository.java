package com.merchant.adapter.persistence.repository.jpa;

import com.merchant.adapter.persistence.entity.StorefrontEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StorefrontJpaRepository extends JpaRepository<StorefrontEntity, Long> {
    Optional<StorefrontEntity> findByUuid(String uuid);

    List<StorefrontEntity> findByMerchantIdOrderByCreatedAtDesc(String merchantId);

    boolean existsBySlug(String slug);

    @Query("""
            select (count(storefront) > 0) from StorefrontEntity storefront
            where storefront.slug = :slug
              and storefront.uuid <> :excludingUuid
            """)
    boolean existsBySlugExcludingUuid(@Param("slug") String slug, @Param("excludingUuid") String excludingUuid);
}
