package com.pricing.infrastructure.service;

import com.pricing.domain.aggregate.Pricing;
import com.pricing.infrastructure.entity.PricingEntity;

public interface PricingService {
    PricingEntity findOrCreate(Pricing pricing);
    void save(PricingEntity pricing);
}
