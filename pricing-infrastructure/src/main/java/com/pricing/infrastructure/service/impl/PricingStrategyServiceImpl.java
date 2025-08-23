package com.pricing.infrastructure.service.impl;

import com.pricing.domain.aggregate.Pricing;
import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;
import com.pricing.infrastructure.entity.PricingEntity;
import com.pricing.infrastructure.entity.PricingStrategyEntity;
import com.pricing.infrastructure.entity.factory.PricingStrategyEntityFactory;
import com.pricing.infrastructure.entity.factory.PricingStrategyEntityFactoryDirector;
import com.pricing.infrastructure.mapper.price.PricingStrategyEntityMapper;
import com.pricing.infrastructure.service.PricingStrategyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PricingStrategyServiceImpl implements PricingStrategyService {
    private final PricingStrategyEntityMapper pricingEntityMapper;
    private final PricingStrategyEntityFactoryDirector pricingStrategyEntityFactoryDirector;

    @Transactional(readOnly = true)
    public void updatePricingStrategies(PricingEntity variantEntity, Pricing pricing) {
        Map<String, AbstractPricingModel<String>> pricingModelMap = pricing.getPricingModels().stream()
                .collect(Collectors.toMap(AbstractPricingModel<String>::getId, Function.identity(), (e1, e2) -> e1, LinkedHashMap::new));
        mergeAndRemovePricing(variantEntity, pricingModelMap);
        addNewPricing(variantEntity, pricingModelMap);
    }

    private void mergeAndRemovePricing(PricingEntity pricingEntity, Map<String, AbstractPricingModel<String>> domainMap) {
        for (PricingStrategyEntity strategy : pricingEntity.getPricingStrategies()) {
            AbstractPricingModel<String> domain = domainMap.get(pricingEntity.getUuid());

            if (Objects.nonNull(domain)) {
                pricingEntityMapper.map(domain, strategy);
                domainMap.remove(domain.getId());
            } else {
                pricingEntity.removeStrategy(strategy);
            }
        }
    }

    private void addNewPricing(PricingEntity pricingEntity, Map<String, AbstractPricingModel<String>> remainingDomainMap) {
        for (AbstractPricingModel<String> strategy : remainingDomainMap.values()) {
            PricingStrategyEntityFactory factory = pricingStrategyEntityFactoryDirector.getFactory(strategy);
            PricingStrategyEntity pricingStrategy = factory.create();
            pricingEntity.addStrategy(pricingStrategy);
        }
    }
}
