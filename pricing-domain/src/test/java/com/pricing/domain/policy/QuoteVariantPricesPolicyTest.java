package com.pricing.domain.policy;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteVariantPricesPolicyTest {

    private final QuoteVariantPricesPolicy policy = new QuoteVariantPricesPolicy();

    @Test
    void validate_validArguments_doesNotThrow() {
        assertThatCode(() -> policy.validate(
                List.of(new CommonId("v-1")),
                "USD",
                new CommonId("c-1"),
                new CommonId("r-1"),
                1
        )).doesNotThrowAnyException();
    }

    @Test
    void validate_nullVariantIds_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> policy.validate(
                null,
                "USD",
                new CommonId("c-1"),
                new CommonId("r-1"),
                1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("variantIds");
    }

    @Test
    void validate_blankVariantId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> policy.validate(
                Collections.singletonList(null),
                "USD",
                new CommonId("c-1"),
                new CommonId("r-1"),
                1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("variantIds[0]");
    }

    @Test
    void validate_blankCurrencyCode_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> policy.validate(
                List.of(new CommonId("v-1")),
                " ",
                new CommonId("c-1"),
                new CommonId("r-1"),
                1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("currencyCode");
    }

    @Test
    void validate_nullSalesChannelId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> policy.validate(
                List.of(new CommonId("v-1")),
                "USD",
                null,
                new CommonId("r-1"),
                1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("salesChannelId");
    }

    @Test
    void validate_nullRegionId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> policy.validate(
                List.of(new CommonId("v-1")),
                "USD",
                new CommonId("c-1"),
                null,
                1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("regionId");
    }

    @Test
    void validate_nonPositiveQuantity_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> policy.validate(
                List.of(new CommonId("v-1")),
                "USD",
                new CommonId("c-1"),
                new CommonId("r-1"),
                0
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity");
    }
}
