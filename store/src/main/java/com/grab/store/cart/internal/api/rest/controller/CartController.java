package com.grab.store.cart.internal.api.rest.controller;

import com.grab.store.cart.internal.api.rest.assembler.CartModelAssembler;
import com.grab.store.cart.internal.api.rest.dto.request.AddItemToCartRequest;
import com.grab.store.cart.internal.api.rest.dto.response.CartResponse;
import com.grab.store.cart.internal.api.rest.service.CartCommandService;
import com.grab.store.cart.internal.api.rest.service.CartQueryService;
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
public class CartController {
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
