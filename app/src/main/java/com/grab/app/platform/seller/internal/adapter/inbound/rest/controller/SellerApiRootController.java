package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.port.rest.SellerRestApi;

import com.grab.app.platform.seller.internal.command.*;
import com.grab.app.platform.seller.internal.query.*;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.app.platform.seller.port.rest.ApiRootApi;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.CatalogRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.InventoryRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.IdentityRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.MerchantRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.PricingRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SalesChannelRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.StorefrontQueryRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.CartRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.RegionRootController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.WorkflowsRootController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class SellerApiRootController implements ApiRootApi , SellerRestApi {

    @Override
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerApiRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(CatalogRootController.class).root()).withRel("get-catalog-root"));
        model.add(linkTo(methodOn(InventoryRootController.class).root()).withRel("get-inventory-root"));
        model.add(linkTo(methodOn(IdentityRootController.class).root()).withRel("get-identity-root"));
        model.add(linkTo(methodOn(MerchantRootController.class).root()).withRel("get-merchant-root"));
        model.add(linkTo(methodOn(PricingRootController.class).root()).withRel("get-pricing-root"));
        model.add(linkTo(methodOn(SalesChannelRootController.class).root()).withRel("get-sales-channel-root"));
        model.add(linkTo(methodOn(StorefrontQueryRootController.class).root()).withRel("get-storefront-query-root"));
        model.add(linkTo(methodOn(CartRootController.class).root()).withRel("get-cart-root"));
        model.add(linkTo(methodOn(RegionRootController.class).root()).withRel("get-region-root"));
        model.add(linkTo(methodOn(WorkflowsRootController.class).root()).withRel("get-workflows-root"));
        return ResponseEntity.ok(model);
    }
}
