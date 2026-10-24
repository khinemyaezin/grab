package com.grab.store.storefrontquery.internal.api.rest.controller;

import com.grab.store.storefrontquery.internal.api.rest.assembler.BuyableOfferModelAssembler;
import com.grab.store.storefrontquery.internal.api.rest.assembler.ShoppingContextModelAssembler;
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
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final ShoppingContextModelAssembler contextAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<BuyableOfferResponse>>> list(
            @RequestHeader(value = "X-Publishable-Key", required = false) String publishableKey,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            Pageable pageable,
            PagedResourcesAssembler<BuyableOfferResponse> pagedAssembler
    ) {
        Page<BuyableOfferResponse> page = storefrontQueryService.list(
                publishableKey,
                countryCode,
                locale,
                keyword,
                categoryId,
                pageable
        );
        Link navigationBase = linkTo(methodOn(StorefrontProductController.class)
                .list(null, countryCode, locale, keyword, categoryId, null, null))
                .withSelfRel();
        PagedModel<EntityModel<BuyableOfferResponse>> model = pagedAssembler.toModel(page, assembler, navigationBase);
        Link listLink = linkTo(methodOn(StorefrontProductController.class)
                .list(null, countryCode, locale, keyword, categoryId, pageable, null))
                .withRel("list-storefront-products");
        model.add(listLink);
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(model);
    }

    @GetMapping("/context")
    public ResponseEntity<EntityModel<ShoppingContextResponse>> resolveContext(
            @RequestHeader(value = "X-Publishable-Key", required = false) String publishableKey,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String locale
    ) {
        ShoppingContextResponse response = storefrontQueryService.resolveShoppingContext(
                publishableKey,
                countryCode,
                locale
        );
        EntityModel<ShoppingContextResponse> model = contextAssembler.toModel(response);
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(model);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<EntityModel<BuyableOfferResponse>> getBySlug(
            @PathVariable String slug,
            @RequestHeader(value = "X-Publishable-Key", required = false) String publishableKey,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String locale
    ) {
        BuyableOfferResponse response = storefrontQueryService.getBySlug(
                publishableKey,
                countryCode,
                locale,
                slug
        );
        EntityModel<BuyableOfferResponse> model = assembler.toModel(response);
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(model);
    }
}
