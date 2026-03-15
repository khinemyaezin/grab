package com.catalog.domain.exception;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.service.impl.DefaultVariantCombinationService;
import com.catalog.domain.support.CatalogDomainLoggerExtension;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(CatalogDomainLoggerExtension.class)
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
    void product_invalidTransition_shouldThrowTypedValidationError() {
        Product product = Product.create(id("p-1"), "Product", id("c-1"));

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.ARCHIVED))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> {
                    CatalogDomainValidationException typed = (CatalogDomainValidationException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.domain.invalid_product_status_transition");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    @Test
    void variantCombination_tooManyCombinations_shouldThrowTypedValidationError() {
        VariantCombinationService service = new DefaultVariantCombinationService();
        List<VariantType> variantTypes = createVariantTypesWithCombinationsOverLimit();

        assertThatThrownBy(() -> service.generateCombinations(variantTypes))
                .isInstanceOf(CatalogDomainValidationException.class)
                .satisfies(exception -> {
                    CatalogDomainValidationException typed = (CatalogDomainValidationException) exception;
                    assertThat(typed.getMessageSource().code()).isEqualTo("cat.domain.too_many_variant_combinations");
                    assertThat(typed.getMessageSource().kind()).isEqualTo(ErrorCategory.BUSINESS_RULE);
                });
    }

    private static List<VariantType> createVariantTypesWithCombinationsOverLimit() {
        List<VariantType> types = new ArrayList<>();
        for (int type = 0; type < 6; type++) {
            VariantType variantType = new VariantType(id("type-" + type), "Type-" + type);
            for (int option = 0; option < 10; option++) {
                variantType.addOption(new VariantOption(
                        id("opt-" + type + "-" + option),
                        "Option-" + option,
                        variantType
                ));
            }
            types.add(variantType);
        }
        return types;
    }

    private static Id id(String value) {
        return () -> value;
    }
}
