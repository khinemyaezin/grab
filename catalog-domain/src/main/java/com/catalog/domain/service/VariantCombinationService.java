package com.catalog.domain.service;

import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;

import java.util.List;

public interface VariantCombinationService {
    /**
     * IMPLEMENTATION REQUIREMENTS:
     *
     * R1: ORDER PRESERVATION
     *    - MUST preserve the exact input order of variant types in output combinations
     *    - MUST preserve the exact declared order of options within each variant type
     *
     * REQUIRED BEHAVIOR:
     * Given input:
     * Color.options = [Red, Blue, Green] // declared order
     * Size.options  = [S, M, L] // declared order
     *
     * Output:
     * [Red, S]
     * [Red, M]
     * [Red, L]
     * [Blue, S]
     * [Blue, M]
     * [Blue, L]
     * [Green, S]
     * [Green, M]
     * [Green, L]
     * @param variantTypes ordered list of variant types where position determines
     *                    combination structure. Order is preserved.
     * @return combinations that strictly maintain input ordering
     * @throws IllegalArgumentException if requirement R4 (100,000 limit) is violated
     */
    List<List<VariantOption>> generateCombinations(List<VariantType> variantTypes);
}
