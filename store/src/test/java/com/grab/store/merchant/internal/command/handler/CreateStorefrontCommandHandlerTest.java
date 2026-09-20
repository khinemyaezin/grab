package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.CreateStorefrontCommand;
import com.merchant.application.service.CreateStorefrontService;
import com.grab.store.merchant.support.MerchantAccountRepositoryStub;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.enums.StorefrontStatus;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.domain.service.StorefrontProvisioningService;
import com.merchant.domain.service.StorefrontSlugPolicy;
import com.merchant.domain.valueobject.BusinessRegistration;
import com.merchant.domain.valueobject.ContactInformation;
import com.merchant.domain.valueobject.MerchantName;
import com.merchant.domain.valueobject.RegisteredAddress;
import com.merchant.domain.valueobject.StorefrontSlug;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateStorefrontServiceTest {
    private final Instant now = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    void execute_whenMerchantIsOperational_shouldCreateDraft() {
        MerchantAccountRepositoryStub merchants = new MerchantAccountRepositoryStub();
        merchants.save(operationalMerchant());
        StorefrontRepositoryStub storefronts = new StorefrontRepositoryStub();
        CreateStorefrontService service = service(merchants, storefronts);

        var result = service.execute(new CreateStorefrontCommand(
                new CommonId("merchant-1"), "Main Shop", "main-shop"));

        assertThat(result.status()).isEqualTo(StorefrontStatus.DRAFT.name());
        assertThat(result.slug()).isEqualTo("main-shop");
        assertThat(storefronts.findById(new CommonId(result.storefrontId()))).isPresent();
    }

    @Test
    void execute_whenSlugTaken_shouldReject() {
        MerchantAccountRepositoryStub merchants = new MerchantAccountRepositoryStub();
        merchants.save(operationalMerchant());
        StorefrontRepositoryStub storefronts = new StorefrontRepositoryStub();
        storefronts.slugTaken = true;
        CreateStorefrontService service = service(merchants, storefronts);

        assertThatThrownBy(() -> service.execute(new CreateStorefrontCommand(
                new CommonId("merchant-1"), "Main Shop", "main-shop")))
                .isInstanceOf(MerchantDomainException.class);
    }

    private CreateStorefrontService service(
            MerchantAccountRepositoryStub merchants,
            StorefrontRepositoryStub storefronts
    ) {
        return new CreateStorefrontService(
                merchants,
                storefronts,
                new StorefrontProvisioningService(),
                new StorefrontSlugPolicy(storefronts),
                new IdGenerator() {
                    @Override
                    public Id generateId() {
                        return new CommonId("sf-1");
                    }

                    @Override
                    public Id convertIdFrom(String id) {
                        return new CommonId(id);
                    }
                }
        );
    }

    private MerchantAccount operationalMerchant() {
        MerchantAccount merchant = MerchantAccount.startDraft(
                new CommonId("merchant-1"),
                new CommonId("applicant-1"),
                MerchantType.FIRST_PARTY_RETAILER,
                "Shop",
                now
        );
        merchant.updateProfile(
                new MerchantName("Legal Name", "Shop"),
                new BusinessRegistration("MM", "REG-1"),
                new ContactInformation("owner@example.com", "+959111111111"),
                new RegisteredAddress("1 Main", null, "Yangon", "Yangon", "11181", "MM"),
                now
        );
        merchant.submit(new CommonId("applicant-1"), now);
        merchant.approve(new CommonId("reviewer-1"), now);
        return merchant;
    }

    private static final class StorefrontRepositoryStub implements StorefrontRepository {
        private final List<Storefront> storefronts = new ArrayList<>();
        private boolean slugTaken;

        @Override
        public Optional<Storefront> findById(Id id) {
            return storefronts.stream().filter(storefront -> storefront.getId().equals(id)).findFirst();
        }

        @Override
        public List<Storefront> findByMerchantId(Id merchantId) {
            return storefronts.stream().filter(storefront -> storefront.getMerchantId().equals(merchantId)).toList();
        }

        @Override
        public boolean existsSlug(StorefrontSlug slug, Id excludingId) {
            return slugTaken;
        }

        @Override
        public Storefront save(Storefront storefront) {
            storefronts.removeIf(existing -> existing.getId().equals(storefront.getId()));
            storefronts.add(storefront);
            return storefront;
        }
    }
}
