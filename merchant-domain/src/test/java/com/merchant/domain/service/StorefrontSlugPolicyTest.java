package com.merchant.domain.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.repository.StorefrontRepository;
import com.merchant.domain.valueobject.StorefrontSlug;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorefrontSlugPolicyTest {
    @Test
    void requireAvailable_whenSlugExists_shouldReject() {
        StorefrontSlugPolicy policy = new StorefrontSlugPolicy(repository(true));

        assertThatThrownBy(() -> policy.requireAvailable(new StorefrontSlug("taken-shop"), new CommonId("sf-1")))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void requireAvailable_whenSlugIsFree_shouldPass() {
        StorefrontSlugPolicy policy = new StorefrontSlugPolicy(repository(false));

        assertThatCode(() -> policy.requireAvailable(new StorefrontSlug("free-shop"), null))
                .doesNotThrowAnyException();
    }

    private StorefrontRepository repository(boolean exists) {
        return new StorefrontRepository() {
            @Override
            public Optional<Storefront> findById(Id id) {
                return Optional.empty();
            }

            @Override
            public List<Storefront> findByMerchantId(Id merchantId) {
                return List.of();
            }

            @Override
            public boolean existsSlug(StorefrontSlug slug, Id excludingId) {
                return exists;
            }

            @Override
            public Storefront save(Storefront storefront) {
                return storefront;
            }
        };
    }
}
