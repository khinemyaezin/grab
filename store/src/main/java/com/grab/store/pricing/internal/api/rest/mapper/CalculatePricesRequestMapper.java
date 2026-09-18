package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.request.CalculatePricesRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.CalculatedPriceSetResponse;
import com.grab.store.pricing.internal.query.CalculatePricesQuery;
import com.grab.store.pricing.internal.query.CalculatedPriceSetResult;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CalculatePricesRequestMapper {

    @Autowired
    protected IdMapper idMapper;

    public CalculatePricesQuery toQuery(CalculatePricesRequest request) {
        List<Id> priceSetIds = request.priceSetIds().stream()
                .map(idMapper::map)
                .toList();
        CalculatePricesRequest.PricingContextRequest context = request.context();
        Map<String, String> attributes = context.attributes() == null ? Map.of() : context.attributes();
        return new CalculatePricesQuery(
                priceSetIds,
                context.currencyCode(),
                context.quantity(),
                attributes
        );
    }

    public abstract CalculatedPriceSetResponse toResponse(CalculatedPriceSetResult result);

    public abstract List<CalculatedPriceSetResponse> toResponseList(List<CalculatedPriceSetResult> results);
}
