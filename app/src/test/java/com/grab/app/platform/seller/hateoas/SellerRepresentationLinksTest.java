package com.grab.app.platform.seller.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.AllocationModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.AccessContextModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.CreateSellableProductModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantMemberModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.ProductSummaryModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SellerAuthModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SellerUserProfileModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.StorefrontModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductSummaryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SellerRepresentationLinksTest {

    @Test
    void emptyProductPageRetainsDiscoveryAndQueryContext() {
        var assembler = new ProductSummaryModelAssembler();
        var page = new PageImpl<ProductSummaryResponse>(List.of(), PageRequest.of(2, 5), 10);
        var model = assembler.toPageModel(page, "red & blue", 2, 5);

        assertThat(model.getContent()).isEmpty();
        assertRelations(model, "self", "prev", "list-products", "search-products", "create-product");
        assertThat(model.getRequiredLink("self").getHref()).contains("query=red%20%26%20blue", "page=2", "size=5");
        assertThat(model.getRequiredLink("prev").getHref()).contains("query=red%20%26%20blue", "page=1", "size=5");
        assertThat(model.getRequiredLink("search-products").getVariableNames()).contains("query");
        assertThat(model.getRequiredLink("list-products").getHref()).doesNotContain("query");
    }

    @Test
    void distinguishesWorkflowStatusFromCommandResult() {
        var assembler = new CreateSellableProductModelAssembler();
        var response = new CreateSellableProductResponse("workflow-1", null, null, null, null, null, null, null);
        var read = assembler.toModel(response);
        var result = assembler.toCommandModel(response);

        assertRelations(read, "self", "create-sellable-product");
        assertRelations(result, "get-create-sellable-product", "create-sellable-product");
        assertThat(result.getRequiredLink("get-create-sellable-product").getHref())
                .isEqualTo(read.getRequiredLink("self").getHref())
                .endsWith("/create-sellable-product/workflow-1");

        var missingId = new CreateSellableProductResponse(null, null, null, null, null, null, null, null);
        assertRelations(assembler.toCommandModel(missingId), "create-sellable-product");
    }

    @Test
    void preservesStorefrontActionsAndEmptyCollectionCreation() {
        var assembler = new StorefrontModelAssembler();
        var draft = new StorefrontResponse("shop", "merchant", null, null, "DRAFT", null, null, null, 0);
        var closed = new StorefrontResponse("shop", "merchant", null, null, "CLOSED", null, null, null, 0);

        assertRelations(assembler.toCommandModel(draft), "get-storefront", "list-storefronts",
                "create-storefront", "update-storefront", "close-storefront", "activate-storefront");
        assertRelations(assembler.toModel(closed), "self", "list-storefronts", "create-storefront");

        var empty = assembler.listStorefrontsModel(List.of(), "merchant");
        assertRelations(empty, "self", "list-storefronts", "create-storefront");
        assertThat(empty.getRequiredLink("create-storefront").getHref()).endsWith("/merchants/merchant/storefronts");
    }

    @Test
    void preservesMerchantApplicationAndMemberStateActions() {
        var merchants = new MerchantModelAssembler();
        var draft = new MerchantResponse("merchant", null, "DRAFT", null, null, null, null);
        var pending = new MerchantResponse("merchant", null, "PENDING_REVIEW", null, null, null, null);
        assertRelations(merchants.toCommandModel(draft), "get-merchant", "list-merchants",
                "update-merchant-profile", "submit-merchant-application");
        assertRelations(merchants.toModel(pending), "self", "list-merchants");

        var members = new MerchantMemberModelAssembler();
        var invited = new MerchantMemberResponse("member", "merchant", null, null, "INVITED", null, null, null, null, null, 0);
        var active = new MerchantMemberResponse("member", "merchant", null, null, "ACTIVE", null, null, null, null, null, 0);
        assertRelations(members.toModel(invited), "list-merchant-members", "accept-merchant-member-invitation");
        assertRelations(members.toModel(active), "list-merchant-members", "update-merchant-member-role", "remove-merchant-member");
    }

    @Test
    void commandOnlyRepresentationsExposeActionsWithoutSelf() {
        var allocation = new AllocationResponse("reservation", null, null, null, 1, null, null);
        var auth = new SellerAuthResponse(null, null, 0, "user", null, null, null, false);
        var registration = new SellerUserProfileResponse("user", null, null, null);

        assertRelations(new AllocationModelAssembler().toModel(allocation), "allocate-stock", "deallocate-stock");
        assertRelations(new SellerAuthModelAssembler().toModel(auth), "refresh", "logout");
        assertRelations(new SellerUserProfileModelAssembler().toModel(registration), "login");
    }

    @Test
    void accessContextRepresentationsExposeCanonicalSelectionRelation() {
        var response = new AccessContextResponse(
                "assignment-1", Set.of("SELLER"), "MERCHANT", "merchant-1", null, "Merchant", null
        );

        var model = new AccessContextModelAssembler().toModel(response);

        assertRelations(model, "select-context");
        assertThat(model.getRequiredLink("select-context").getHref())
                .endsWith("/identity/session/contexts/assignment-1/select");
    }

    private static void assertRelations(RepresentationModel<?> model, String... expected) {
        assertThat(model.getLinks()).extracting(Link::getRel).extracting(Object::toString)
                .containsExactlyInAnyOrder(expected);
    }
}
