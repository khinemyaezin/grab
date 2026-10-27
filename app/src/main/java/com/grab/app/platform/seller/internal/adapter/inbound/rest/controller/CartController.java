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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.assembler.CartModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.dto.request.AddItemToCartRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.cart.rest.dto.response.CartResponse;
import com.grab.app.platform.seller.internal.command.CartCommandService;
import com.grab.app.platform.seller.internal.query.CartQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController implements SellerRestApi {
    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;
    private final CartModelAssembler assembler;

    @PostMapping("/lines")
    public ResponseEntity<EntityModel<CartResponse>> addItem(
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @RequestBody AddItemToCartRequest request
    ) {
        return ResponseEntity.ok(assembler.toModel(cartCommandService.addItem(guestToken, request)));
    }

    @GetMapping("/current")
    public ResponseEntity<EntityModel<CartResponse>> current(
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @RequestParam String salesChannelId,
            @RequestParam(required = false) String regionId
    ) {
        return ResponseEntity.ok(assembler.toModel(cartQueryService.current(guestToken, salesChannelId, regionId)));
    }
}
