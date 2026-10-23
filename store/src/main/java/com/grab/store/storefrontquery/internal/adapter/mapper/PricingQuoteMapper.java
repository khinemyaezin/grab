package com.grab.store.storefrontquery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.port.PricingQuoteQuery;
import com.grab.store.storefrontquery.internal.api.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class PricingQuoteMapper {
    public abstract PricingPort.QuotedPrice toQuotedPrice(PricingQuoteQuery.QuotedPrice quote);
}
