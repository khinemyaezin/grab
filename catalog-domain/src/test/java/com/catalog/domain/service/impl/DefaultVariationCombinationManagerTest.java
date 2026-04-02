package com.catalog.domain.service.impl;

import com.catalog.domain.aggregate.ProductVariant;
import com.grab.framework.id.Id;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.VariantCombination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultVariationCombinationManagerTest {
    private VariationCombinationManager variationCombinationManager;

    /**
    productId    | sku      | size    | color   | gender
    -------------|----------|---------|---------|----------
    product-1    | SKU-LYM  | Large   | Yellow  | Male
    product-1    | SKU-LYF  | Large   | Yellow  | Female
    product-1    | SKU-LRM  | Large   | Red     | Male
    product-1    | SKU-LRF  | Large   | Red     | Female
    product-1    | SKU-SYM  | Small   | Yellow  | Male
    product-1    | SKU-SYF  | Small   | Yellow  | Female
    product-1    | SKU-SRM  | Small   | Red     | Male
    product-1    | SKU-SRF  | Small   | Red     | Female
    */
    private List<ProductVariant> getProductVariation() {
        return List.of(
                variant(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),

                variant(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender"))),

                variant(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),

                variant(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender"))),

                variant(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),

                variant(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender"))),

                variant(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),

                variant(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")))
        );
    }

    protected static ProductVariant variant(ProductVariation... variations) {
        return new ProductVariant(null, null, ProductVariantStatus.ACTIVE, List.of(variations));
    }

    protected static ProductVariation variation(Id optionId, Id typeId) {
        return new ProductVariation(optionId, typeId);
    }

    protected static VariantCombination combination(ProductVariation... variations) {
        return new VariantCombination(List.of(variations));
    }

    private String extractTemplateFrom(VariationCombinationManager.VariantCombinationResult variantCombinationResult) {
        return String.format("%s %s %s",
                variantCombinationResult.variantCombination().variations().stream()
                        .map( variation -> variation.getOptionId().getValue())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining()),
                variantCombinationResult.matchedVariant() == null ? "ACTIVE" : variantCombinationResult.matchedVariant().getStatus(),
                variantCombinationResult.matchedType());
    }

    record CommonId(String id) implements Id {
        @Override
        public String getValue() {
            return this.id;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CommonId(String id1))) return false;
            return Objects.equals(id, id1);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    @BeforeEach
    public void init() {
        var keyFactory = new DefaultVariationKeyGenerator(new ProductVariationComparator());
        variationCombinationManager = new DefaultVariationCombinationManager(keyFactory);
    }

    @Test
    public void syncCombinations_addVariantType_returnNewVariants() {
        List<ProductVariant> existingVariants = List.of();
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),
                combination(
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender"))),
                combination(
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),
                combination(
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")))
        );
        String[] desiredCombination = {
                "color-yellowgender-male ACTIVE NEW",
                "color-yellowgender-female ACTIVE NEW",
                "color-redgender-male ACTIVE NEW",
                "color-redgender-female ACTIVE NEW"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtFirst_returnCombinationInOrder() {
        List<ProductVariant> existingVariants = getProductVariation();
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),
                combination(
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender"))),
                combination(
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),
                combination(
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")))
        );
        String[] desiredCombination = {
                "color-yellowgender-male ACTIVE EXTENDED",
                "color-yellowgender-female ACTIVE EXTENDED",
                "color-redgender-male ACTIVE EXTENDED",
                "color-redgender-female ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtMiddle_returnCombinationInOrder() {
        List<ProductVariant> existingVariants = getProductVariation();
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("gender-female"), new CommonId("gender"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("gender-male"), new CommonId("gender"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("gender-female"), new CommonId("gender")))
        );
        String[] desiredCombination = {
                "size-largegender-male ACTIVE EXTENDED",
                "size-largegender-female ACTIVE EXTENDED",
                "size-smallgender-male ACTIVE EXTENDED",
                "size-smallgender-female ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantTypeAtLast_returnCombinationInOrder() {
        List<ProductVariant> existingVariants = getProductVariation();
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")))
        );
        String[] desiredCombination = {
                "size-largecolor-yellow ACTIVE EXTENDED",
                "size-largecolor-red ACTIVE EXTENDED",
                "size-smallcolor-yellow ACTIVE EXTENDED",
                "size-smallcolor-red ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_addVariantTypeAtLast_returnCombinationsInOrder_() {
        List<ProductVariant> existingVariants = getProductVariation();
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender")),
                        variation(new CommonId("storage-128"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender")),
                        variation(new CommonId("storage-256"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")),
                        variation(new CommonId("storage-128"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")),
                        variation(new CommonId("storage-256"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender")),
                        variation(new CommonId("storage-128"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender")),
                        variation(new CommonId("storage-256"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")),
                        variation(new CommonId("storage-128"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")),
                        variation(new CommonId("storage-256"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender")),
                        variation(new CommonId("storage-128"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender")),
                        variation(new CommonId("storage-256"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")),
                        variation(new CommonId("storage-128"), new CommonId("storage"))),
                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")),
                        variation(new CommonId("storage-256"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender")),
                        variation(new CommonId("storage-128"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-male"), new CommonId("gender")),
                        variation(new CommonId("storage-256"), new CommonId("storage"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")),
                        variation(new CommonId("storage-128"), new CommonId("storage"))),
                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("gender-female"), new CommonId("gender")),
                        variation(new CommonId("storage-256"), new CommonId("storage")))
        );
        String[] desiredCombination = {
                "size-largecolor-yellowgender-malestorage-128 ACTIVE EXTENDED",
                "size-largecolor-yellowgender-malestorage-256 ACTIVE EXTENDED",
                "size-largecolor-yellowgender-femalestorage-128 ACTIVE EXTENDED",
                "size-largecolor-yellowgender-femalestorage-256 ACTIVE EXTENDED",
                "size-largecolor-redgender-malestorage-128 ACTIVE EXTENDED",
                "size-largecolor-redgender-malestorage-256 ACTIVE EXTENDED",
                "size-largecolor-redgender-femalestorage-128 ACTIVE EXTENDED",
                "size-largecolor-redgender-femalestorage-256 ACTIVE EXTENDED",
                "size-smallcolor-yellowgender-malestorage-128 ACTIVE EXTENDED",
                "size-smallcolor-yellowgender-malestorage-256 ACTIVE EXTENDED",
                "size-smallcolor-yellowgender-femalestorage-128 ACTIVE EXTENDED",
                "size-smallcolor-yellowgender-femalestorage-256 ACTIVE EXTENDED",
                "size-smallcolor-redgender-malestorage-128 ACTIVE EXTENDED",
                "size-smallcolor-redgender-malestorage-256 ACTIVE EXTENDED",
                "size-smallcolor-redgender-femalestorage-128 ACTIVE EXTENDED",
                "size-smallcolor-redgender-femalestorage-256 ACTIVE EXTENDED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_addVariantOption_returnCombinationsInOrder() {
        List<ProductVariant> existingVariants = List.of(
                variant(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color"))),

                variant(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color"))),

                variant(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color"))),

                variant(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")))
        );
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color"))),

                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-blue"), new CommonId("color"))),

                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color"))),

               combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color"))),
                combination(
                        variation(new CommonId("size-small"), new CommonId("size")),
                        variation(new CommonId("color-blue"), new CommonId("color")))
        );

        String[] desiredCombination = {
                "size-largecolor-yellow ACTIVE UNCHANGED",
                "size-largecolor-red ACTIVE UNCHANGED",
                "size-largecolor-blue ACTIVE NEW",
                "size-smallcolor-yellow ACTIVE UNCHANGED",
                "size-smallcolor-red ACTIVE UNCHANGED",
                "size-smallcolor-blue ACTIVE NEW"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_removeVariantOption_returnCombinationsInOrder() {
        List<ProductVariant> existingVariants = List.of(
                variant(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color"))),

                variant(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")))
        );
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color")))
        );

        String[] desiredCombination = {
                "size-largecolor-yellow ACTIVE UNCHANGED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }

    @Test
    public void syncCombinations_changeVariantOptionOrder_returnCombinationsInOrder() {
        List<ProductVariant> existingVariants = List.of(
                variant(
                        variation(new CommonId("color-yellow"), new CommonId("color")),
                        variation(new CommonId("size-large"), new CommonId("size"))),


                variant(
                        variation(new CommonId("color-red"), new CommonId("color")),
                        variation(new CommonId("size-large"), new CommonId("size")))
        );
        List<VariantCombination> combinations = List.of(
                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-yellow"), new CommonId("color"))),
                combination(
                        variation(new CommonId("size-large"), new CommonId("size")),
                        variation(new CommonId("color-red"), new CommonId("color")))
        );

        String[] desiredCombination = {
                "size-largecolor-yellow ACTIVE UNCHANGED",
                "size-largecolor-red ACTIVE UNCHANGED"
        };
        List<VariationCombinationManager.VariantCombinationResult>  results = variationCombinationManager.syncCombinations(existingVariants, combinations);
        assertThat(results)
                .extracting(this::extractTemplateFrom)
                .containsExactly(desiredCombination );
    }


}
