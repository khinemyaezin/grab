package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CalculatePriceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePriceListRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CalculatedPriceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Public REST Contract for Pricing Strategies, Price Lists, and Preferences.
 */
public interface SellerPriceListResource {

    ResponseEntity<EntityModel<PriceListResponse>> createPriceList(CreatePriceListRequest request);

    ResponseEntity<EntityModel<PriceListResponse>> getPriceList(String priceListId);

    ResponseEntity<CollectionModel<EntityModel<PriceListResponse>>> listPriceLists();

    /**
     * Public REST Contract for Price Sets and Calculations.
     */
    interface PriceSets {
        ResponseEntity<EntityModel<PriceSetResponse>> createPriceSet();
        ResponseEntity<EntityModel<PriceSetResponse>> getPriceSet(String priceSetId);
        ResponseEntity<List<CalculatedPriceResponse>> calculatePrices(CalculatePriceRequest request);
    }

    /**
     * Public REST Contract for Merchant Pricing Preferences.
     */
    interface Preferences {
        ResponseEntity<EntityModel<PricePreferenceResponse>> createPreference(CreatePricePreferenceRequest request);
        ResponseEntity<EntityModel<PricePreferenceResponse>> getPreference(String preferenceId);
    }
}
