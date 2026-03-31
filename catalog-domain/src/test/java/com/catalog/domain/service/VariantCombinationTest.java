package com.catalog.domain.service;

import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.grab.framework.id.Id;
import com.catalog.domain.service.impl.DefaultVariantCombinationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        var idColor = id("color");
        var yellow = new VariantOptionSelection(id("yellow"), idColor);
        var red = new VariantOptionSelection(id("red"), idColor);
        var color = new VariantTypeSelection(idColor, List.of(yellow, red));

        var idSize = id("size");
        var small = new VariantOptionSelection(id("small"),idSize);
        var large = new VariantOptionSelection(id("large"), idSize);
        var size = new VariantTypeSelection(idSize, List.of(small, large));

        List<List<VariantOptionSelection>> combinations = variantCombination.generateCombinations(List.of(color, size));

        List<List<VariantOptionSelection>> expected = List.of(
                List.of(yellow, small),
                List.of(yellow, large),
                List.of(red, small),
                List.of(red, large)
        );
        assertThat(combinations)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void returnsEmptyWhenMissingOptions() {
        var color = new VariantTypeSelection(id("color"), List.of());
        // no options added

        assertThat(variantCombination.generateCombinations(List.of(color))).isEmpty();
        assertThat(variantCombination.generateCombinations(List.of())).isEmpty();
        assertThat(variantCombination.generateCombinations(null)).isEmpty();
    }

    private Id id(String value) {
        return () -> value;
    }
}
