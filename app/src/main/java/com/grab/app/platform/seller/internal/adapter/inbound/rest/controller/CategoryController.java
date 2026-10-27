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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler.*;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request.SaveCategoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.*;
import com.grab.app.platform.seller.internal.command.CategoryCommandService;
import com.grab.app.platform.seller.internal.query.CategoryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/catalog/categories")
@RequiredArgsConstructor
public class CategoryController implements SellerRestApi {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;
    private final CategoryModelAssembler categoryModelAssembler;
    private final CategoryNodeModelAssembler categoryNodeModelAssembler;
    private final CategoryChildrenModelAssembler categoryChildrenModelAssembler;
    private final CategoryLeavesModelAssembler categoryLeavesModelAssembler;
    private final DeleteCategoryModelAssembler deleteCategoryModelAssembler;

    @PostMapping()
    public ResponseEntity<Void> saveCategory(@Valid @RequestBody SaveCategoryRequest request) {
        String categoryId = categoryCommandService.saveCategory(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoryId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "/{categoryId}")
    public ResponseEntity<EntityModel<CategoryResponse>> getCategory(@PathVariable String categoryId) {
        CategoryResponse response = categoryQueryService.getCategory(categoryId);
        return ResponseEntity.ok(categoryModelAssembler.toModel(response));
    }

    @GetMapping(value = "/{categoryId}/tree")
    public ResponseEntity<EntityModel<CategoryNodeResponse>> getCategoryTree(@PathVariable String categoryId) {
        CategoryNodeResponse response = categoryQueryService.getCategoryTree(categoryId);
        return ResponseEntity.ok(categoryNodeModelAssembler.toModel(response));
    }

    @GetMapping(value = "/{categoryId}/parent")
    public ResponseEntity<EntityModel<CategoryResponse>> getCategoryParent(@PathVariable String categoryId) {
        CategoryResponse response = categoryQueryService.getCategoryParent(categoryId);
        return ResponseEntity.ok(categoryModelAssembler.toModel(response));
    }

    @GetMapping(value = "/{categoryId}/children")
    public ResponseEntity<EntityModel<CategoryChildrenResponse>> getCategoryChildren(@PathVariable String categoryId) {
        CategoryChildrenResponse response = categoryQueryService.getCategoryChildren(categoryId);
        return ResponseEntity.ok(categoryChildrenModelAssembler.toModel(response));
    }

    @GetMapping(value = "/leaves")
    public ResponseEntity<EntityModel<CategoryLeavesResponse>> getLeafNodesByName(@RequestParam("name") String name) {
        CategoryLeavesResponse response = categoryQueryService.getLeafNodesByName(name);
        return ResponseEntity.ok(categoryLeavesModelAssembler.toModel(response, name));
    }

    @DeleteMapping(value = "/{categoryId}")
    public ResponseEntity<EntityModel<DeleteCategoryResponse>> deleteCategory(@PathVariable String categoryId) {
        DeleteCategoryResponse response = categoryCommandService.deleteCategory(categoryId);

        if (response.deleted()) {
            return ResponseEntity.ok(deleteCategoryModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }
}
