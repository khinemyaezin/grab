package com.grab.store.storefrontquery.internal.api.rest.controller;

import com.grab.store.storefrontquery.internal.api.rest.assembler.BuyableOfferModelAssembler;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.ShoppingContextResponse;
import com.grab.store.storefrontquery.internal.api.rest.service.StorefrontProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/storefront-query/products")
@RequiredArgsConstructor
public class StorefrontProductController {
    private final StorefrontProductQueryService storefrontQueryService;
    private final BuyableOfferModelAssembler assembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<BuyableOfferResponse>>> list(
            @RequestParam String salesChannelId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            Pageable pageable,
            PagedResourcesAssembler<BuyableOfferResponse> pagedAssembler
    ) {
        Page<BuyableOfferResponse> page = storefrontQueryService.list(salesChannelId, keyword, categoryId, pageable);
        PagedModel<EntityModel<BuyableOfferResponse>> model = pagedAssembler.toModel(page, assembler);
        Link listLink = linkTo(methodOn(StorefrontProductController.class)
                .list(salesChannelId, keyword, categoryId, pageable, null))
                .withRel("list-storefront-products");
        model.add(listLink);
        return ResponseEntity.ok(model);
    }

    @GetMapping("/context")
    public ResponseEntity<ShoppingContextResponse> resolveContext(
            @RequestParam String publishableKey,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String locale
    ) {
        ShoppingContextResponse response = storefrontQueryService.resolveShoppingContext(publishableKey, countryCode, locale);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<EntityModel<BuyableOfferResponse>> get(
            @PathVariable String slug,
            @RequestParam String salesChannelId
    ) {
        BuyableOfferResponse response = storefrontQueryService.getBySlug(salesChannelId, slug);
        EntityModel<BuyableOfferResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }
}
