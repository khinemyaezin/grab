package com.grab.service.contract.message.merchandising.pricing;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Universal Java Port Interface for Pricing Quotes across Checkout and Storefront.
 * Replaces legacy PricingQuoteQuery.
 */
public interface PricingQuotePort extends Remote {

    QuotedPriceResult quote(
            Id variantId,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId
    ) throws RemoteException;

    default QuotedPriceResult quote(Id variantId, String currencyCode, int quantity, Id salesChannelId) throws RemoteException {
        return quote(variantId, currencyCode, quantity, salesChannelId, null);
    }

    BatchQuoteResult quoteBatch(BatchQuoteRequest message) throws RemoteException;

    record QuotedPriceResult(
            BigDecimal amount,
            BigDecimal originalAmount,
            String currencyCode,
            Id priceSetId
    ) implements Serializable {}

    record BatchQuoteRequest(
            List<Id> variantIds,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId
    ) implements Serializable {
        public BatchQuoteRequest {
            variantIds = variantIds == null ? List.of() : List.copyOf(variantIds);
        }
    }

    record BatchQuoteResult(Map<Id, QuotedPriceResult> quotes) implements Serializable {
        public BatchQuoteResult {
            quotes = quotes == null ? Map.of() : Map.copyOf(quotes);
        }
    }
}

