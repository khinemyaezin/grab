package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SaveCategoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryNodeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Seller Central Category hierarchy management.
 */
public interface SellerCategoryResource {

    ResponseEntity<EntityModel<CategoryResponse>> saveCategory(SaveCategoryRequest request);

    ResponseEntity<EntityModel<CategoryResponse>> getCategory(String categoryId);

    ResponseEntity<EntityModel<CategoryNodeResponse>> getCategoryTree(String rootId);

    ResponseEntity<CollectionModel<EntityModel<CategoryResponse>>> getChildren(String categoryId);

    ResponseEntity<CollectionModel<EntityModel<CategoryResponse>>> getLeafNodes(String name);

    ResponseEntity<Void> deleteCategory(String categoryId);
}
