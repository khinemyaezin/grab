package com.grab.discovery.application.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShoppingContextResolutionPolicyTest {
    private final ShoppingContextResolutionPolicy policy = new ShoppingContextResolutionPolicy();

    @Test
    void normalizeCountry_trimsAndUppercasesIsoCode() {
        assertThat(policy.normalizeCountry(" sg ")).isEqualTo("SG");
    }

    @Test
    void normalizeCountry_rejectsUnknownIsoCode() {
        assertThatThrownBy(() -> policy.normalizeCountry("ZZ"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normalizeLocale_returnsCanonicalLanguageTag() {
        assertThat(policy.normalizeLocale(" en-sg ")).isEqualTo("en-SG");
    }

    @Test
    void normalizeLocale_rejectsPreferenceListsAndWildcards() {
        assertThatThrownBy(() -> policy.normalizeLocale("en-SG,en;q=0.8"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy.normalizeLocale("*"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normalizeCurrency_uppercasesAndValidatesIsoCode() {
        assertThat(policy.normalizeCurrency("usd")).isEqualTo("USD");
        assertThatThrownBy(() -> policy.normalizeCurrency("US"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
