package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.entity.VariantPriceSetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VariantPriceSetLinkJpaRepository extends JpaRepository<VariantPriceSetLinkEntity, String> {

    List<VariantPriceSetLinkEntity> findByVariantIdIn(Collection<String> variantIds);

    void deleteByPriceSetId(String priceSetId);
}
