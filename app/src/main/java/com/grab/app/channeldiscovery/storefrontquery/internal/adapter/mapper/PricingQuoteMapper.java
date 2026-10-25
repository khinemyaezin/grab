package com.grab.app.channeldiscovery.storefrontquery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.productmerchandising.pricing.port.PricingQuoteQuery;
import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class PricingQuoteMapper {
    public abstract PricingPort.QuotedPrice toQuotedPrice(PricingQuoteQuery.QuotedPrice quote);
}
