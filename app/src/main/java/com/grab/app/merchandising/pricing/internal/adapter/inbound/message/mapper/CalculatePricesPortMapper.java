package com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.pricing.port.message.PriceSetPort.CalculatePriceMessage;
import com.grab.app.merchandising.pricing.port.message.PriceSetPort.CalculatedPriceMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.pricing.application.model.read.CalculatePricesQuery;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import org.mapstruct.Mapper;

import java.util.Map;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CalculatePricesPortMapper {

    public CalculatePricesQuery toQuery(CalculatePriceMessage message) {
        if (message == null) {
            return null;
        }
        Map<String, String> attrs = message.attributes() != null ? message.attributes() : Map.of();
        return new CalculatePricesQuery(
                message.priceSetIds(),
                message.currencyCode(),
                message.quantity(),
                attrs
        );
    }

    public abstract CalculatedPriceMessageResult toResponse(CalculatedPriceSetResult result);
}

