package com.pricing.infrastructure.repository.facade;

import com.pricing.domain.aggregate.Pricing;
import com.pricing.domain.repository.PricingRepository;
import com.pricing.infrastructure.entity.PricingEntity;
import com.pricing.infrastructure.service.PricingService;
import com.pricing.infrastructure.service.PricingStrategyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PricingFacadeRepository implements PricingRepository {
    private final PricingStrategyService pricingEntityService;
    private final PricingService pricingService;

    @Transactional
    @Override
    public Pricing save(Pricing pricing) {
      PricingEntity entity = pricingService.findOrCreate(pricing);
      pricingEntityService.updatePricingStrategies(entity,pricing);
      pricingService.save(entity);
      return pricing;
    }

    @Override
    public void delete(Pricing pricing) {

    }

    @Override
    public Pricing getPricing(String uuid) {
        return null;
    }
}
