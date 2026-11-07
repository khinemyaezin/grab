package com.grab.app.platform.seller.contract;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.*;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SellerModelAssemblersContractTest {

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createSellableProductModelAssembler_shouldExposeRequiredLinks() {
        CreateSellableProductModelAssembler assembler = new CreateSellableProductModelAssembler();
        CreateSellableProductResponse response = new CreateSellableProductResponse(
                "wf-1", "COMPLETED", "COMPLETED", "prod-1", List.of(), List.of(), null, Instant.now()
        );

        EntityModel<CreateSellableProductResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("get-create-sellable-product")).isPresent();
        assertThat(model.getLink("create-sellable-product")).isPresent();
    }

    @Test
    void updateSellableProductModelAssembler_shouldExposeRequiredLinks() {
        UpdateSellableProductModelAssembler assembler = new UpdateSellableProductModelAssembler();
        UpdateSellableProductResponse response = new UpdateSellableProductResponse(
                "wf-1", "COMPLETED", "COMPLETED", "prod-1", true, List.of(), List.of(), 0, List.of(), List.of(), false, null
        );

        EntityModel<UpdateSellableProductResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("get-update-sellable-product")).isPresent();
        assertThat(model.getLink("update-sellable-product")).isPresent();
    }

    @Test
    void updateProductVariantModelAssembler_shouldExposeRequiredLinks() {
        UpdateProductVariantModelAssembler assembler = new UpdateProductVariantModelAssembler();
        UpdateProductVariantResponse response = new UpdateProductVariantResponse(
                "wf-1", "COMPLETED", "COMPLETED", "prod-1", "var-1", "SKU-1", true, null, List.of(), 0, false, null
        );

        EntityModel<UpdateProductVariantResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("get-update-product-variant")).isPresent();
        assertThat(model.getLink("update-product-variant")).isPresent();
    }

    @Test
    void productModelAssembler_shouldExposeSearchAndLifecycleLinks() {
        ProductModelAssembler assembler = new ProductModelAssembler();
        ProductResponse response = new ProductResponse(
                "prod-1", "T-Shirt", "cat-1", "Clothing", "NEW", "DRAFT", "t-shirt", List.of()
        );

        EntityModel<ProductResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("search-products")).isPresent();
        assertThat(model.getLink("update-product-status")).isPresent();
        assertThat(model.getLink("get-category")).isPresent();
    }

    @Test
    void facilityLocationModelAssembler_shouldExposeFacilityActionsWithoutFacilityPrefix() {
        FacilityLocationModelAssembler assembler = new FacilityLocationModelAssembler();
        FacilityLocationResponse response = new FacilityLocationResponse(
                "loc-1", "mer-1", "LOC", "Warehouse", "WAREHOUSE", true
        );

        EntityModel<FacilityLocationResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("activate-location")).isPresent();
        assertThat(model.getLink("deactivate-location")).isPresent();
        assertThat(model.getLink("delete-location")).isPresent();
    }

    @Test
    void facilityZoneModelAssembler_shouldExposeZoneActionsWithoutFacilityPrefix() {
        FacilityZoneModelAssembler assembler = new FacilityZoneModelAssembler();
        FacilityZoneResponse response = new FacilityZoneResponse(
                "zone-1", "loc-1", "ZN", "Zone A", "STORAGE", true
        );

        EntityModel<FacilityZoneResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("activate-zone")).isPresent();
        assertThat(model.getLink("deactivate-zone")).isPresent();
        assertThat(model.getLink("delete-zone")).isPresent();
    }

    @Test
    void facilityBinModelAssembler_shouldExposeBinActionsWithoutFacilityPrefix() {
        FacilityBinModelAssembler assembler = new FacilityBinModelAssembler();
        FacilityBinResponse response = new FacilityBinResponse(
                "bin-1", "zone-1", "BN", "Bin 1", 100, true
        );

        EntityModel<FacilityBinResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("activate-bin")).isPresent();
        assertThat(model.getLink("deactivate-bin")).isPresent();
        assertThat(model.getLink("delete-bin")).isPresent();
    }

    @Test
    void allocationModelAssembler_shouldExposeAllocateAndDeallocateLinks() {
        AllocationModelAssembler assembler = new AllocationModelAssembler();
        AllocationResponse response = new AllocationResponse(
                "res-1", "ord-1", "SKU-1", "loc-1", 5, "RESERVED", LocalDateTime.now()
        );

        EntityModel<AllocationResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("deallocate")).isPresent();
        assertThat(model.getLink("allocate")).isPresent();
    }

    @Test
    void priceSetModelAssembler_shouldExposeCalculatePricesLink() {
        PriceSetModelAssembler assembler = new PriceSetModelAssembler();
        PriceSetResponse response = new PriceSetResponse("ps-1", List.of());

        EntityModel<PriceSetResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("calculate-prices")).isPresent();
    }

    @Test
    void merchantModelAssembler_withDraftApplication_shouldExposeEditAndSubmitLinks() {
        MerchantModelAssembler assembler = new MerchantModelAssembler();
        MerchantResponse response = new MerchantResponse(
                "mer-1", "BUSINESS", "DRAFT", "Acme Inc", "Acme", Instant.now(), Instant.now()
        );

        EntityModel<MerchantResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("list-my-merchants")).isPresent();
        assertThat(model.getLink("edit-merchant-application")).isPresent();
        assertThat(model.getLink("submit-merchant-application")).isPresent();
    }

    @Test
    void storefrontModelAssembler_withDraft_shouldExposeActivateAndLifecycleLinks() {
        StorefrontModelAssembler assembler = new StorefrontModelAssembler();
        StorefrontResponse response = new StorefrontResponse(
                "sf-1", "mer-1", "My Store", "my-store", "DRAFT", null,
                Instant.now(), Instant.now(), 0L
        );

        EntityModel<StorefrontResponse> model = assembler.toModel(response);

        assertThat(model.getLink("self")).isPresent();
        assertThat(model.getLink("list-storefronts")).isPresent();
        assertThat(model.getLink("create-storefront")).isPresent();
        assertThat(model.getLink("update-storefront")).isPresent();
        assertThat(model.getLink("close-storefront")).isPresent();
        assertThat(model.getLink("activate-storefront")).isPresent();
    }

    @Test
    void merchantMemberModelAssembler_shouldExposeLifecycleLinks() {
        MerchantMemberModelAssembler assembler = new MerchantMemberModelAssembler();

        MerchantMemberResponse invited = new MerchantMemberResponse(
                "mem-1", "mer-1", "usr-1", "OPERATOR", "INVITED",
                null, null, Instant.now(), Instant.now(), Instant.now(), 0L
        );
        EntityModel<MerchantMemberResponse> invitedModel = assembler.toModel(invited);
        assertThat(invitedModel.getLink("list-merchant-members")).isPresent();
        assertThat(invitedModel.getLink("accept-invitation")).isPresent();
        assertThat(invitedModel.getLink("change-role")).isEmpty();
        assertThat(invitedModel.getLink("remove-member")).isEmpty();

        MerchantMemberResponse active = new MerchantMemberResponse(
                "mem-1", "mer-1", "usr-1", "OPERATOR", "ACTIVE",
                null, null, Instant.now(), Instant.now(), Instant.now(), 0L
        );
        EntityModel<MerchantMemberResponse> activeModel = assembler.toModel(active);
        assertThat(activeModel.getLink("accept-invitation")).isEmpty();
        assertThat(activeModel.getLink("change-role")).isPresent();
        assertThat(activeModel.getLink("remove-member")).isPresent();
    }

    @Test
    void publishableKeyModelAssembler_shouldExposeRevokePublishableKeyLink() {
        PublishableKeyModelAssembler assembler = new PublishableKeyModelAssembler();
        PublishableKeyResponse response = new PublishableKeyResponse(
                "key-1", "ch-1", "Test Key", "raw_token", "pk_", Instant.now()
        );

        EntityModel<PublishableKeyResponse> model = assembler.toModel(response);

        assertThat(model.getLink("revoke-publishable-key")).isPresent();
    }
}
