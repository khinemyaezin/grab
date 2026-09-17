package com.merchant.domain.valueobject;

import com.merchant.domain.exception.MerchantDomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorefrontValueObjectTest {
    @Test
    void storefrontName_trimsAndRejectsBlank() {
        assertThat(new StorefrontName("  Main Shop  ").value()).isEqualTo("Main Shop");
        assertThatThrownBy(() -> new StorefrontName("  "))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void storefrontSlug_normalizesToKebabCaseAndRejectsInvalid() {
        assertThat(new StorefrontSlug("Main-Shop").value()).isEqualTo("main-shop");
        assertThatThrownBy(() -> new StorefrontSlug("Main Shop"))
                .isInstanceOf(MerchantDomainException.class);
        assertThatThrownBy(() -> new StorefrontSlug("-leading"))
                .isInstanceOf(MerchantDomainException.class);
    }
}
