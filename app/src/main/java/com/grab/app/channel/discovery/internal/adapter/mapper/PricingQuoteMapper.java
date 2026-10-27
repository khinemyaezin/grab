package com.grab.app.channel.discovery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.catalog.pricing.port.PricingQuoteQuery;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class PricingQuoteMapper {
    public abstract PricingPort.QuotedPrice toQuotedPrice(PricingQuoteQuery.QuotedPrice quote);
}
