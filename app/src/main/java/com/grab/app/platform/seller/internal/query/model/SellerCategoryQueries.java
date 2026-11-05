package com.grab.app.platform.seller.internal.query.model;

import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryNodeOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.framework.cqrs.query.Query;

import java.util.List;
import java.util.Optional;

public final class SellerCategoryQueries {

    private SellerCategoryQueries() {}

    public record GetSellerCategoryQuery(
            String categoryId
    ) implements Query<Optional<CategoryOutboundResult>> {}

    public record GetSellerCategoryTreeQuery(
            String rootId
    ) implements Query<CategoryNodeOutboundResult> {}

    public record GetSellerCategoryChildrenQuery(
            String categoryId
    ) implements Query<List<CategoryOutboundResult>> {}

    public record GetSellerCategoryLeavesQuery(
            String name
    ) implements Query<List<CategoryOutboundResult>> {}
}

