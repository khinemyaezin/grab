package com.grab.service.sellerbff.internal.query.model;

import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductSummaryOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOptionOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantTypeOutboundResult;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public final class SellerProductQueries {

    private SellerProductQueries() {}

    public record GetSellerProductQuery(
            Id merchantId,
            Id productId
    ) implements Query<Optional<ProductOutboundResult>> {}

    public record GetSellerProductBySlugQuery(
            String slug
    ) implements Query<Optional<ProductOutboundResult>> {}

    public record SearchSellerProductsQuery(
            Id merchantId,
            String query,
            String variantStatus,
            Id categoryId,
            String productStatus,
            int page,
            int size
    ) implements Query<List<ProductSummaryOutboundResult>> {}

    public record GetSellerVariantQuery(
            Id merchantId,
            Id productId,
            Id variantId
    ) implements Query<Optional<VariantOutboundResult>> {}

    public record ListSellerVariantTypesQuery(
            String name
    ) implements Query<List<VariantTypeOutboundResult>> {}

    public record ListSellerVariantOptionsQuery(
            String name,
            String typeId
    ) implements Query<List<VariantOptionOutboundResult>> {}
}

