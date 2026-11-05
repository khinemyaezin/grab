package com.grab.app.platform.seller.port.outbound;

import java.util.List;
import java.util.Optional;

/**
 * Seller Outbound Port for Category taxonomy operations.
 */
public interface SellerCategoryOutboundPort {

    CategoryOutboundResult saveCategory(SaveCategoryRecord record);

    Optional<CategoryOutboundResult> findCategoryById(String categoryId);

    CategoryNodeOutboundResult getCategoryTree(String rootId);

    List<CategoryOutboundResult> findChildren(String categoryId);

    List<CategoryOutboundResult> findLeafCategories(String name);

    void deleteCategory(String categoryId);

    record SaveCategoryRecord(
            String name,
            String parentId,
            Boolean active,
            Boolean listingAllowed,
            Boolean c2cAllowed
    ) {}

    record CategoryOutboundResult(
            String id,
            String name,
            String parentId,
            boolean active,
            boolean listingAllowed,
            boolean c2cAllowed
    ) {}

    record CategoryNodeOutboundResult(
            String id,
            String name,
            String slug,
            List<CategoryNodeOutboundResult> children
    ) {}
}
