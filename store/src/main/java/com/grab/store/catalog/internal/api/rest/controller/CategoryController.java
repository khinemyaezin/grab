package com.grab.store.catalog.internal.api.rest.controller;

import com.grab.store.catalog.internal.api.rest.dto.request.SaveCategoryRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryChildrenResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryLeavesResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryNodeResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteCategoryResponse;
import com.grab.store.catalog.internal.api.rest.service.CategoryFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryFacadeService categoryFacadeService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveCategory(@Valid @RequestBody SaveCategoryRequest request) {
        String categoryId = categoryFacadeService.saveCategory(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoryId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<CategoryResponse>> getCategory(@PathVariable("categoryId") String categoryId) {
        EntityModel<CategoryResponse> response = categoryFacadeService.getCategory(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{categoryId}/tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<CategoryNodeResponse>> getCategoryTree(@PathVariable("categoryId") String categoryId) {
        EntityModel<CategoryNodeResponse> response = categoryFacadeService.getCategoryTree(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{categoryId}/parent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<CategoryResponse>> getCategoryParent(@PathVariable("categoryId") String categoryId) {
        EntityModel<CategoryResponse> response = categoryFacadeService.getCategoryParent(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{categoryId}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<CategoryChildrenResponse>> getCategoryChildren(@PathVariable("categoryId") String categoryId) {
        EntityModel<CategoryChildrenResponse> response = categoryFacadeService.getCategoryChildren(categoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/leaves", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<CategoryLeavesResponse>> getLeafNodesByName(@RequestParam("name") String name) {
        EntityModel<CategoryLeavesResponse> response = categoryFacadeService.getLeafNodesByName(name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<DeleteCategoryResponse>> deleteCategory(@PathVariable("categoryId") String categoryId) {
        EntityModel<DeleteCategoryResponse> response = categoryFacadeService.deleteCategory(categoryId);

        if (response.getContent() != null && response.getContent().deleted()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}
