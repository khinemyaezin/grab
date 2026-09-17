package com.grab.store.storefront.internal.api.rest.controller;

import com.grab.store.storefront.internal.api.rest.assembler.StorefrontCategoryChildrenModelAssembler;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontCategoryChildrenResponse;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontCategoryNodeResponse;
import com.grab.store.storefront.internal.api.rest.service.StorefrontBrowseService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/storefront/categories")
@RequiredArgsConstructor
public class StorefrontCategoryController {

    private final StorefrontBrowseService browseService;
    private final StorefrontCategoryChildrenModelAssembler childrenAssembler;

    @GetMapping("/tree")
    public ResponseEntity<List<StorefrontCategoryNodeResponse>> tree(
            @RequestParam(value = "rootId", required = false) String rootId
    ) {
        return ResponseEntity.ok(browseService.categoryTree(rootId));
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<EntityModel<StorefrontCategoryChildrenResponse>> children(
            @PathVariable String categoryId
    ) {
        return ResponseEntity.ok(childrenAssembler.toModel(browseService.categoryChildren(categoryId)));
    }
}
