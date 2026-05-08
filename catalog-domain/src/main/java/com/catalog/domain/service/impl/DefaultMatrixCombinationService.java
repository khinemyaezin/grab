package com.catalog.domain.service.impl;

import com.catalog.domain.exception.CatalogDomainError;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultMatrixCombinationService implements MatrixCombinationService {

    private static final Logger log = Loggers.getLogger(DefaultMatrixCombinationService.class);

    public List<List<VariantOptionSelection>> generateMatrixCombination(List<VariantTypeSelection> variantTypes) {
        if (variantTypes == null || variantTypes.isEmpty()) {
            log.debug("Skipping variant combination generation because no variant types were provided");
            return Collections.emptyList();
        }

        log.info("Generating variant combinations for {} variant types", variantTypes.size());

        List<List<VariantOptionSelection>> optionLists = new ArrayList<>(variantTypes.size());
        int totalCombinations = 1;

        for (VariantTypeSelection variantType : variantTypes) {
            if (variantType == null || variantType.options().isEmpty()) {
                log.warn("Skipping combination generation because a variant type is null or has no options");
                return Collections.emptyList();
            }
            List<VariantOptionSelection> options = new ArrayList<>(variantType.options());
            optionLists.add(options);
            totalCombinations *= options.size();

            if (totalCombinations > 100_000) {
                log.warn("Rejected variant combination generation because totalCombinations={} exceeds limit={}", totalCombinations, 100_000);
                throw new CatalogDomainValidationException(
                        new CatalogDomainError.TooManyVariantCombinations(totalCombinations, 100_000),
                        "Too many combinations: " + totalCombinations + ". Consider filtering options."
                );
            }
        }

        List<List<VariantOptionSelection>> combinations = generateIterativeCombinations(optionLists, totalCombinations);
        log.info("Generated {} variant combinations", combinations.size());
        return combinations;
    }

    private List<List<VariantOptionSelection>> generateIterativeCombinations(
            List<List<VariantOptionSelection>> optionLists, int totalCombinations) {

        List<List<VariantOptionSelection>> combinations = new ArrayList<>(totalCombinations);

        for (int i = 0; i < totalCombinations; i++) {
            combinations.add(new ArrayList<>(optionLists.size()));
        }

        int period = totalCombinations;
        for (List<VariantOptionSelection> options : optionLists) {
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
