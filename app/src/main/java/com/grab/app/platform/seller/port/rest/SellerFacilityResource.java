package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityBinRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityLocationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityZoneRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Warehouse Facilities (Locations, Zones, Bins).
 */
public interface SellerFacilityResource {

    ResponseEntity<EntityModel<FacilityLocationResponse>> createLocation(CreateFacilityLocationRequest request, String merchantId, String actorId);

    ResponseEntity<EntityModel<FacilityLocationResponse>> getLocation(String locationId);

    ResponseEntity<Void> activateLocation(String locationId);

    ResponseEntity<Void> deactivateLocation(String locationId);

    ResponseEntity<Void> deleteLocation(String locationId);

    /**
     * Public REST Contract for Facility Storage Zones.
     */
    interface Zones {
        ResponseEntity<EntityModel<FacilityZoneResponse>> createZone(String locationId, CreateFacilityZoneRequest request, String actorId);
        ResponseEntity<EntityModel<FacilityZoneResponse>> getZone(String zoneId);
        ResponseEntity<Void> activateZone(String zoneId);
        ResponseEntity<Void> deactivateZone(String zoneId);
        ResponseEntity<Void> deleteZone(String zoneId);
    }

    /**
     * Public REST Contract for Storage Bins inside Zones.
     */
    interface Bins {
        ResponseEntity<EntityModel<FacilityBinResponse>> createBin(String zoneId, CreateFacilityBinRequest request, String actorId);
        ResponseEntity<EntityModel<FacilityBinResponse>> getBin(String binId);
        ResponseEntity<Void> activateBin(String binId);
        ResponseEntity<Void> deactivateBin(String binId);
        ResponseEntity<Void> deleteBin(String binId);
    }
}
