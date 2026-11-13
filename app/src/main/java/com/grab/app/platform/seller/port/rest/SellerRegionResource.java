package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionCountryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

public interface SellerRegionResource {

    ResponseEntity<EntityModel<RegionResponse>> get(String regionId);

    ResponseEntity<CollectionModel<EntityModel<RegionCountryResponse>>> countries();
}

