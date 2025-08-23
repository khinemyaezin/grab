package com.pricing.infrastructure.service.impl;

import com.pricing.domain.aggregate.Pricing;
import com.pricing.infrastructure.entity.PricingEntity;
import com.pricing.infrastructure.entity.factory.PricingEntityFactory;
import com.pricing.infrastructure.mapper.price.PricingEntityMapper;
import com.pricing.infrastructure.repository.jpa.PricingJpaRepository;
import com.pricing.infrastructure.service.PricingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PricingServiceImpl implements PricingService {
    private final PricingJpaRepository pricingJpaRepository;
    private final PricingEntityFactory pricingEntityFactory;
    private final PricingEntityMapper pricingEntityMapper;

    @Transactional(readOnly = true)
    public PricingEntity findOrCreate(Pricing pricing) {
        return pricingJpaRepository.findByProduct(pricing.getProductId())
                .map(entity-> {
                    pricingEntityMapper.map(pricing, entity);
                    return entity;
                })
                .orElseGet(()-> {
                    PricingEntity entity = this.pricingEntityFactory.create();
                    pricingEntityMapper.map(pricing,entity);
                    return entity;
                });
    }

    @Transactional
    public void save(PricingEntity pricing) {
        pricingJpaRepository.save(pricing);
    }
}
