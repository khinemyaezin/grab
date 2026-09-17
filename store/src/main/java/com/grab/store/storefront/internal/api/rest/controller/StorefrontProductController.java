package com.grab.store.storefront.internal.api.rest.controller;

import com.grab.store.storefront.internal.api.rest.assembler.StorefrontProductCardModelAssembler;
import com.grab.store.storefront.internal.api.rest.assembler.StorefrontProductDetailModelAssembler;
import com.grab.store.storefront.internal.api.rest.dto.request.StorefrontProductSearchRequest;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductCard;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductDetail;
import com.grab.store.storefront.internal.api.rest.service.StorefrontBrowseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storefront/products")
@RequiredArgsConstructor
public class StorefrontProductController {

    private final StorefrontBrowseService browseService;
    private final StorefrontProductCardModelAssembler cardAssembler;
    private final StorefrontProductDetailModelAssembler detailAssembler;

    @PostMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<StorefrontProductCard>>> search(
            @RequestBody(required = false) @Valid StorefrontProductSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<StorefrontProductCard> pagedResourcesAssembler
    ) {
        Page<StorefrontProductCard> page = browseService.search(request, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, cardAssembler));
    }

    @GetMapping("/featured")
    public ResponseEntity<PagedModel<EntityModel<StorefrontProductCard>>> featured(
            @PageableDefault(size = 10) Pageable pageable,
            PagedResourcesAssembler<StorefrontProductCard> pagedResourcesAssembler
    ) {
        Page<StorefrontProductCard> page = browseService.featured(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, cardAssembler));
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<PagedModel<EntityModel<StorefrontProductCard>>> newArrivals(
            @PageableDefault(size = 10) Pageable pageable,
            PagedResourcesAssembler<StorefrontProductCard> pagedResourcesAssembler
    ) {
        Page<StorefrontProductCard> page = browseService.newArrivals(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, cardAssembler));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<EntityModel<StorefrontProductDetail>> bySlug(@PathVariable String slug) {
        return ResponseEntity.ok(detailAssembler.toModel(browseService.bySlug(slug)));
    }
}
