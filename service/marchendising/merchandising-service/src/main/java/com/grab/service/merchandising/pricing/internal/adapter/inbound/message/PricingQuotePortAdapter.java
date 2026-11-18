package com.grab.service.merchandising.pricing.internal.adapter.inbound.message;

import com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper.QuoteBatchPricesPortMapper;
import com.grab.service.merchandising.pricing.internal.adapter.inbound.message.mapper.QuotePricePortMapper;
import com.grab.service.contract.message.merchandising.pricing.PricingQuotePort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.framework.exception.DomainException;
import com.grab.service.merchandising.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.QuoteVariantPriceResult;
import com.pricing.application.port.inbound.QuoteVariantPriceUseCase;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.rmi.RemoteException;
import java.util.Optional;

@Component
@RmiExport
public class PricingQuotePortAdapter implements PricingQuotePort {

    private final QueryBus queryBus;
    private final QuotePricePortMapper quotePriceMapper;
    private final QuoteVariantPriceUseCase quoteVariantPrice;
    private final QuoteBatchPricesPortMapper quoteBatchPricesMapper;

    public PricingQuotePortAdapter(
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            QuotePricePortMapper quotePriceMapper,
            QuoteVariantPriceUseCase quoteVariantPrice,
            QuoteBatchPricesPortMapper quoteBatchPricesMapper) {
        this.queryBus = queryBus;
        this.quotePriceMapper = quotePriceMapper;
        this.quoteVariantPrice = quoteVariantPrice;
        this.quoteBatchPricesMapper = quoteBatchPricesMapper;
    }

    @Override
    @PricingReadTransactional
    public QuotedPriceResult quote(
            Id variantId,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId) throws RemoteException {
        if (variantId == null) {
            return null;
        }
        QuoteVariantPriceQuery query = quotePriceMapper.toQuery(
                variantId, currencyCode, quantity, salesChannelId, regionId
        );
        try {
            Optional<QuoteVariantPriceResult> result = quoteVariantPrice.execute(query);
            return result.map(quotePriceMapper::toResponse).orElse(null);
        } catch (DomainException exception) {
            throw toRemoteException(exception);
        }
    }

    @Override
    public BatchQuoteResult quoteBatch(BatchQuoteRequest message) throws RemoteException {
        QuoteVariantPricesQuery query = quoteBatchPricesMapper.toQuery(message);
        try {
            QuoteVariantPricesResult result = queryBus.dispatch(query);
            return quoteBatchPricesMapper.toResponse(result);
        } catch (DomainException exception) {
            throw toRemoteException(exception);
        }
    }

    private RemoteException toRemoteException(DomainException exception) {
        String code = exception.getMessageSource().code();
        return new RemoteException(code + ": " + exception.getMessage());
    }
}
