package com.pricing.adapter.persistence.repository.jpa;

import com.pricing.adapter.persistence.entity.VariantPriceSetLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VariantPriceSetLinkJpaRepository extends JpaRepository<VariantPriceSetLinkEntity, String> {

    List<VariantPriceSetLinkEntity> findByVariantIdIn(Collection<String> variantIds);

    List<VariantPriceSetLinkEntity> findByPriceSetId(String priceSetId);

    void deleteByPriceSetId(String priceSetId);
}
