package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.CategoryModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.CategoryNodeModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SaveCategoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryNodeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerCategoryCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerCategoryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class SellerCategoryController {

    private final SellerCategoryCommandService categoryCommandService;
    private final SellerCategoryQueryService categoryQueryService;
    private final CategoryModelAssembler categoryModelAssembler;
    private final CategoryNodeModelAssembler categoryNodeModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<CategoryResponse>> saveCategory(
            @Valid @RequestBody SaveCategoryRequest request) {
        CategoryResponse response = categoryCommandService.saveCategory(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(categoryModelAssembler.toCommandModel(response));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<EntityModel<CategoryResponse>> getCategory(@PathVariable String categoryId) {
        return categoryQueryService.getCategory(categoryId)
                .map(categoryModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tree")
    public ResponseEntity<EntityModel<CategoryNodeResponse>> getCategoryTree(
            @RequestParam(required = false) String rootId) {
        CategoryNodeResponse tree = categoryQueryService.getCategoryTree(rootId);
        return ResponseEntity.ok(categoryNodeModelAssembler.toTreeModel(tree, rootId));
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<CollectionModel<EntityModel<CategoryResponse>>> getChildren(
            @PathVariable String categoryId) {
        List<CategoryResponse> children = categoryQueryService.getChildren(categoryId);
        return ResponseEntity.ok(categoryModelAssembler.getChildrenModel(children, categoryId));
    }

    @GetMapping("/leaf-nodes")
    public ResponseEntity<CollectionModel<EntityModel<CategoryResponse>>> getLeafNodes(
            @RequestParam(required = false) String name) {
        List<CategoryResponse> leaves = categoryQueryService.getLeafCategories(name);
        return ResponseEntity.ok(categoryModelAssembler.getLeafNodesModel(leaves, name));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId) {
        categoryCommandService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
