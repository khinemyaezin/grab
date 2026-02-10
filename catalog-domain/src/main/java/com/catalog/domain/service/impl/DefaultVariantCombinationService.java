package com.catalog.domain.service.impl;

import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.service.VariantCombinationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultVariantCombinationService implements VariantCombinationService {

    public List<List<VariantOption>> generateCombinations(List<VariantType> variantTypes) {
        // Early return for empty input
        if (variantTypes == null || variantTypes.isEmpty())
            return Collections.emptyList();

        // Pre-process and validate in single pass
        List<List<VariantOption>> optionLists = new ArrayList<>(variantTypes.size());
        int totalCombinations = 1;

        for (VariantType variantType : variantTypes) {
            if (variantType == null || variantType.getOptions().isEmpty()) {
                return Collections.emptyList();
            }
            List<VariantOption> options = new ArrayList<>(variantType.getOptions());
            optionLists.add(options);
            totalCombinations *= options.size();

            // Safety check for combinatorial explosion
            if (totalCombinations > 100_000) {
                throw new IllegalArgumentException(
                        "Too many combinations: " + totalCombinations + ". Consider filtering options.");
            }
        }

        // Generate combinations iteratively
        return generateIterativeCombinations(optionLists, totalCombinations);
    }

    private List<List<VariantOption>> generateIterativeCombinations(
            List<List<VariantOption>> optionLists, int totalCombinations) {

        List<List<VariantOption>> combinations = new ArrayList<>(totalCombinations);

        // Initialize with empty lists
        for (int i = 0; i < totalCombinations; i++) {
            combinations.add(new ArrayList<>(optionLists.size()));
        }

        int period = totalCombinations;
        for (List<VariantOption> options : optionLists) {
            period /= options.size();
            int optionIndex = 0;

            for (int i = 0; i < totalCombinations; i++) {
                if (i > 0 && i % period == 0) {
                    optionIndex = (optionIndex + 1) % options.size();
                }
                combinations.get(i).add(options.get(optionIndex));
            }
        }

        return combinations;
    }
}
