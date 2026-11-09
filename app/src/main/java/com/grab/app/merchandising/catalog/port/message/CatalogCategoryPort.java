package com.grab.app.merchandising.catalog.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Catalog Categories.
 * Replaces the legacy Category REST Controller.
 */
public interface CatalogCategoryPort {

    CategoryMessageResult createCategory(CreateCategoryMessage message);

    void deleteCategory(Id categoryId);

    Optional<CategoryMessageResult> findCategoryById(Id categoryId);

    CategoryTreeNodeResult getCategoryTree(Id rootCategoryId);

    List<CategoryMessageResult> getChildCategories(Id categoryId);

    List<CategoryMessageResult> getLeafCategories(String name);

    record CreateCategoryMessage(
            String name,
            Id parentId,
            Boolean active,
            Boolean listingAllowed,
            Boolean c2cAllowed
    ) {}

    record CategoryMessageResult(
            String id,
            String name,
            String parentId,
            boolean active,
            boolean listingAllowed,
            boolean c2cAllowed
    ) {}

    record CategoryTreeNodeResult(
            String id,
            String name,
            String parentId,
            List<CategoryTreeNodeResult> children
    ) {}
}
