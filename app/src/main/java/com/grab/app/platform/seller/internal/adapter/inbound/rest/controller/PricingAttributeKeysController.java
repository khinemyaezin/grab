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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.PricingAttributeKeysResponse;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
import com.pricing.domain.valueobject.PricingAttributeKeys;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@PricingEnabled
@RequestMapping("/api/v1/pricing/attribute-keys")
public class PricingAttributeKeysController implements SellerRestApi {

    @GetMapping
    public ResponseEntity<EntityModel<PricingAttributeKeysResponse>> listAttributeKeys() {
        PricingAttributeKeysResponse response = new PricingAttributeKeysResponse(
                PricingAttributeKeys.wellKnown(),
                PricingAttributeKeys.taxPreferenceLookupOrder()
        );
        EntityModel<PricingAttributeKeysResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(PricingAttributeKeysController.class).listAttributeKeys()).withSelfRel());
        return ResponseEntity.ok(model);
    }
}
