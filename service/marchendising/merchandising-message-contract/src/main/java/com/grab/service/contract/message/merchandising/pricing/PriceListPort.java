package com.grab.service.contract.message.merchandising.pricing;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Price Lists and Pricing Rules.
 * Replaces PriceListController and PricingAttributeKeysController.
 */
public interface PriceListPort extends Remote {

    PriceListMessageResult createPriceList(CreatePriceListMessage message) throws RemoteException;

    PriceListMessageResult findPriceListById(Id priceListId) throws RemoteException;

    List<PriceListMessageResult> listPriceLists() throws RemoteException;

    record CreatePriceListMessage(
            String title,
            String description,
            String type,
            Instant startsAt,
            Instant endsAt
    ) implements Serializable {}

    record PriceListMessageResult(
            String id,
            String title,
            String description,
            String status,
            String type,
            Instant startsAt,
            Instant endsAt,
            List<PriceListRuleMessageResult> rules,
            List<PriceEntryMessageResult> prices
    ) implements Serializable {}

    record PriceListRuleMessageResult(
            String id,
            String attribute,
            List<String> values
    ) implements Serializable {}

    record PriceEntryMessageResult(
            String id,
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            String priceSetId,
            String priceListId
    ) implements Serializable {}
}
