package com.merchant.domain.policy;

import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.domain.exception.MerchantDomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorefrontChannelBrandPolicyTest {

    private final StorefrontChannelBrandPolicy policy = new StorefrontChannelBrandPolicy();

    @Test
    void requireAttachable_whenMissing_allowsAttach() {
        assertThatCode(() -> policy.requireAttachable(null, new CommonId("channel-1")))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAttachable_whenSameChannel_allowsIdempotentAttach() {
        StorefrontChannelBrand existing = StorefrontChannelBrand.restore(
                new CommonId("sf-1"), new CommonId("channel-1"));

        assertThatCode(() -> policy.requireAttachable(existing, new CommonId("channel-1")))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAttachable_whenDifferentChannel_rejects() {
        StorefrontChannelBrand existing = StorefrontChannelBrand.restore(
                new CommonId("sf-1"), new CommonId("channel-1"));

        assertThatThrownBy(() -> policy.requireAttachable(existing, new CommonId("channel-2")))
                .isInstanceOf(MerchantDomainException.class);
    }
}
