package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request.ListVariantPriceSetLinksRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.VariantPriceSetLinkResponse;
import com.pricing.application.model.read.ListVariantPriceSetLinksQuery;
import com.pricing.application.model.read.VariantPriceSetLinkResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListVariantPriceSetLinksRequestMapper {

    public ListVariantPriceSetLinksQuery toQuery(ListVariantPriceSetLinksRequest request) {
        List<String> variantIds = request != null && request.variantIds() != null ? request.variantIds() : List.of();
        return toQuery(variantIds);
    }

    public ListVariantPriceSetLinksQuery toQuery(List<String> variantIds) {
        return new ListVariantPriceSetLinksQuery(variantIds);
    }

    public abstract VariantPriceSetLinkResponse toResponse(VariantPriceSetLinkResult result);

    public abstract List<VariantPriceSetLinkResponse> toResponseList(List<VariantPriceSetLinkResult> results);
}
