package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.VariantOption;
import com.product.domain.service.VariantKeyGenerator;
import com.product.domain.service.VariantSorter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DefaultVariantSorter implements VariantSorter {

    private final VariantKeyGenerator keyGenerator;

    @Override
    public Map<String, Integer> buildCombinationOrderMap(List<List<VariantOption>> combinations, List<Id> typeOrder) {
        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < combinations.size(); i++) {
            List<VariantOption> combination = combinations.get(i);
            String key = keyGenerator.generateCombinationKey(combination, typeOrder);
            orderMap.putIfAbsent(key, i);
        }
        return orderMap;
    }

    @Override
    public void sortVariantsByCombinationOrder(Product product,
                                                List<Id> typeOrder,
                                                Map<String, Integer> combinationOrder) {
        product.sortVariants((v1, v2) -> {
            String key1 = keyGenerator.generateVariantKey(v1, typeOrder);
            String key2 = keyGenerator.generateVariantKey(v2, typeOrder);

            int order1 = combinationOrder.getOrDefault(key1, Integer.MAX_VALUE);
            int order2 = combinationOrder.getOrDefault(key2, Integer.MAX_VALUE);

            return Integer.compare(order1, order2);
        });
    }
}