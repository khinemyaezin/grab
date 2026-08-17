package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.store.pricing.internal.api.rest.dto.request.ListVariantPriceSetLinksRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.VariantPriceSetLinkResponse;
import com.grab.store.pricing.internal.query.ListVariantPriceSetLinksQuery;
import com.grab.store.pricing.internal.query.VariantPriceSetLinkResult;
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
