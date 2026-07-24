package com.grab.store.pricing.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.internal.api.rest.dto.response.PricePreferenceResponse;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import com.grab.store.pricing.internal.query.GetPricePreferenceQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetPricePreferenceRequestMapper {

    public abstract GetPricePreferenceQuery toQuery(String pricePreferenceId);

    public abstract PricePreferenceResponse toResponse(PricePreferenceResult result);
}
