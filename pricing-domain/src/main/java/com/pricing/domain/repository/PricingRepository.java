package com.pricing.domain.repository;

import com.pricing.domain.aggregate.Pricing;

public interface PricingRepository {
    Pricing save(Pricing pricing);
    void delete(Pricing pricing);
    Pricing getPricing(String uuid);
}
