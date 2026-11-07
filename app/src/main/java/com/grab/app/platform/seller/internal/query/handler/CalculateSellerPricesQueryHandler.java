package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.CalculateSellerPricesQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CalculatePriceRecord;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CalculatedPriceOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CalculateSellerPricesQueryHandler
        implements QueryHandler<CalculateSellerPricesQuery, List<CalculatedPriceOutboundResult>> {

    private final SellerPricingOutboundPort outboundPort;

    public CalculateSellerPricesQueryHandler(SellerPricingOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<CalculatedPriceOutboundResult> handle(CalculateSellerPricesQuery query) {
        CalculatePriceRecord record = new CalculatePriceRecord(
                query.priceSetIds(),
                query.currencyCode(),
                query.quantity(),
                query.attributes()
        );
        return outboundPort.calculatePrices(record);
    }

    @Override
    public Class<CalculateSellerPricesQuery> getQueryType() {
        return CalculateSellerPricesQuery.class;
    }
}

