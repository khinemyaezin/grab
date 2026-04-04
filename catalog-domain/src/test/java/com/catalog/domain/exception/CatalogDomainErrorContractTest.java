package com.catalog.domain.exception;

import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.catalog.domain.service.impl.DefaultMatrixCombinationService;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogDomainErrorContractTest {

    private static final int MAX_COMBINATIONS = 100_000;

    @Test
    void productStatusErrors_shouldUseCatDomainCodes() {
        CatalogDomainError.InvalidProductStatusTransition transitionError =
                new CatalogDomainError.InvalidProductStatusTransition("DRAFT", "ARCHIVED");
        CatalogDomainError.ProductActivationRequiresActiveVariants activationError =
                new CatalogDomainError.ProductActivationRequiresActiveVariants();

        assertThat(transitionError.kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
        assertThat(transitionError.code()).isEqualTo("cat.domain.invalid_product_status_transition");
        assertThat(transitionError.args())
                .containsEntry("currentStatus", "DRAFT")
                .containsEntry("newStatus", "ARCHIVED");

        assertThat(activationError.kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
        assertThat(activationError.code()).isEqualTo("cat.domain.product_activation_requires_active_variants");
        assertThat(activationError.args()).isEmpty();
    }

    @Test
    void tooManyCombinationsError_shouldExposeLimitAndTotal() {
        CatalogDomainError.TooManyVariantCombinations error =
                new CatalogDomainError.TooManyVariantCombinations(120_000, MAX_COMBINATIONS);

        assertThat(error.kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
        assertThat(error.code()).isEqualTo("cat.domain.too_many_variant_combinations");
        assertThat(error.args())
                .containsEntry("totalCombinations", 120_000)
                .containsEntry("maxAllowed", MAX_COMBINATIONS);
    }

    @Test
    void variantCombination_tooManyCombinations_shouldThrowTypedValidationError() {
        MatrixCombinationService service = new DefaultMatrixCombinationService();
        var variantTypes = createVariantTypesWithCombinationsOverLimit();

        assertThatThrownBy(() -> service.generateMatrixCombination(variantTypes))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> {
                    CatalogDomainValidationException typed = (CatalogDomainValidationException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.domain.too_many_variant_combinations");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    private static List<VariantTypeSelection> createVariantTypesWithCombinationsOverLimit() {
        List<VariantTypeSelection> types = new ArrayList<>();
        for (int type = 0; type < 6; type++) {
            Id idType = id("type-" + type);
            List<VariantOptionSelection>  variantOptions = new ArrayList<>();
            for (int option = 0; option < 10; option++) {
                variantOptions.add(new VariantOptionSelection(id("opt-" + type + "-" + option), idType));
            }
            types.add(new VariantTypeSelection(idType, variantOptions));
        }
        return types;
    }

    private static Id id(String value) {
        return () -> value;
    }
}
