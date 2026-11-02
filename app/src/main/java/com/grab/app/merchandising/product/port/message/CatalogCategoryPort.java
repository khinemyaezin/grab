package com.grab.app.merchandising.product.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Catalog Categories.
 * Replaces the legacy Category REST Controller.
 */
public interface CatalogCategoryPort {

    CategoryResult createCategory(CreateCategoryMessage message);

    CategoryResult updateCategory(Id categoryId, UpdateCategoryMessage message);

    void deleteCategory(Id categoryId);

    Optional<CategoryResult> findCategoryById(Id categoryId);

    CategoryTreeResult getCategoryTree(Id rootCategoryId);

    Optional<CategoryResult> getParentCategory(Id categoryId);

    List<CategoryResult> getChildCategories(Id categoryId);

    List<CategoryResult> getLeafCategories();

    record CreateCategoryMessage(
            Id merchantId,
            String name,
            String slug,
            String description,
            Id parentCategoryId,
            int sortOrder
    ) {}

    record UpdateCategoryMessage(
            String name,
            String slug,
            String description,
            Id parentCategoryId,
            int sortOrder
    ) {}

    record CategoryResult(
            Id id,
            Id merchantId,
            String name,
            String slug,
            String description,
            Id parentCategoryId,
            int sortOrder,
            Instant createdAt
    ) {}

    record CategoryTreeResult(
            CategoryResult root,
            List<CategoryTreeResult> children
    ) {}
}

