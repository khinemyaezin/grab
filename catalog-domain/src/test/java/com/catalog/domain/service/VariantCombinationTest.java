package com.catalog.domain.service;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.service.impl.DefaultVariantCombinationService;
import com.catalog.domain.support.CatalogDomainLoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CatalogDomainLoggerExtension.class)
class VariantCombinationTest {
    private VariantCombinationService variantCombination;

    @BeforeEach
    void setUp() {
        variantCombination = new DefaultVariantCombinationService();
    }

    /**
     * Given input:
     *  Color : [ Yellow, Red ] , Size : [ Small, Large ]
     * Expected output
     * [ Yellow-Small, Yellow-Large, Red-Small, Red-Large ]
     */
    @Test
    void generatesCartesianProductInStableOrder() {
        VariantType color = new VariantType(id("color"), "Color");
        VariantOption yellow = new VariantOption(id("color-yellow"), "Yellow", color);
        VariantOption red = new VariantOption(id("color-red"), "Red", color);
        color.addOption(yellow);
        color.addOption(red);

        VariantType size = new VariantType(id("size"), "Size");
        VariantOption small = new VariantOption(id("size-small"), "Small", size);
        VariantOption large = new VariantOption(id("size-large"), "Large", size);
        size.addOption(small);
        size.addOption(large);

        List<List<VariantOption>> combinations = variantCombination.generateCombinations(List.of(color, size));
        assertThat(combinations)
                .extracting(combo -> combo.getFirst().getName() + "-" + combo.get(1).getName())
                .containsExactly(
                        "Yellow-Small",
                        "Yellow-Large",
                        "Red-Small",
                        "Red-Large"
                );
    }

    @Test
    void returnsEmptyWhenMissingOptions() {
        VariantType color = new VariantType(id("color"), "Color");
        // no options added

        assertThat(variantCombination.generateCombinations(List.of(color))).isEmpty();
        assertThat(variantCombination.generateCombinations(List.of())).isEmpty();
        assertThat(variantCombination.generateCombinations(null)).isEmpty();
    }

    private Id id(String value) {
        return () -> value;
    }
}
