package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.request.ReplacePriceListRulesRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.command.ReplacePriceListRulesCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ReplacePriceListRulesRequestMapper {

    @Mapping(target = "priceListId", source = "priceListId")
    @Mapping(target = "rules", source = "request.rules")
    public abstract ReplacePriceListRulesCommand toCommand(
            String priceListId,
            ReplacePriceListRulesRequest request
    );

    public abstract PriceListResponse toResponse(PriceListResult result);
}
