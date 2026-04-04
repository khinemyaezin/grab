package com.catalog.domain.service;

import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;

import java.util.List;

public interface MatrixCombinationService {
    /**
     * @param variantTypes ordered list of variant types where position determines
     *                    combination structure. Order is preserved.
     * @return combinations that strictly maintain input ordering
     * @throws CatalogDomainValidationException if requirement R4 (100,000 limit) is violated
     */
    List<List<VariantOptionSelection>> generateMatrixCombination(List<VariantTypeSelection> variantTypes);
}
