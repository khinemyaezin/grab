package com.grab.store.storefrontquery.internal.api.rest.controller;

import com.grab.store.storefrontquery.internal.api.rest.assembler.BuyableOfferModelAssembler;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.store.storefrontquery.internal.api.rest.service.StorefrontQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storefront-query/products")
@RequiredArgsConstructor
public class StorefrontProductController {
    private final StorefrontQueryService storefrontQueryService;
    private final BuyableOfferModelAssembler assembler;
    private final PagedResourcesAssembler<BuyableOfferResponse> pagedAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<BuyableOfferResponse>>> list(
            @RequestParam String salesChannelId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                storefrontQueryService.list(salesChannelId, pageable),
                assembler
        ));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<EntityModel<BuyableOfferResponse>> get(
            @PathVariable String slug,
            @RequestParam String salesChannelId
    ) {
        return ResponseEntity.ok(assembler.toModel(storefrontQueryService.getBySlug(salesChannelId, slug)));
    }
}
