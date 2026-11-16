package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerFacilityBinController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerFacilityLocationController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerFacilityZoneController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerFacilityApiLinks {

    private SellerFacilityApiLinks() {
    }

    public static Link getLocation(String locationId) {
        return linkTo(methodOn(SellerFacilityLocationController.class).getLocation(locationId))
                .withRel("get-location");
    }

    public static Link activateLocation(String locationId) {
        return linkTo(methodOn(SellerFacilityLocationController.class).activateLocation(locationId))
                .withRel("activate-location");
    }

    public static Link deactivateLocation(String locationId) {
        return linkTo(methodOn(SellerFacilityLocationController.class).deactivateLocation(locationId))
                .withRel("deactivate-location");
    }

    public static Link deleteLocation(String locationId) {
        return linkTo(methodOn(SellerFacilityLocationController.class).deleteLocation(locationId))
                .withRel("delete-location");
    }

    public static Link getZone(String zoneId) {
        return linkTo(methodOn(SellerFacilityZoneController.class).getZone(zoneId))
                .withRel("get-zone");
    }

    public static Link activateZone(String zoneId) {
        return linkTo(methodOn(SellerFacilityZoneController.class).activateZone(zoneId))
                .withRel("activate-zone");
    }

    public static Link deactivateZone(String zoneId) {
        return linkTo(methodOn(SellerFacilityZoneController.class).deactivateZone(zoneId))
                .withRel("deactivate-zone");
    }

    public static Link deleteZone(String zoneId) {
        return linkTo(methodOn(SellerFacilityZoneController.class).deleteZone(zoneId))
                .withRel("delete-zone");
    }

    public static Link getBin(String binId) {
        return linkTo(methodOn(SellerFacilityBinController.class).getBin(binId))
                .withRel("get-bin");
    }

    public static Link activateBin(String binId) {
        return linkTo(methodOn(SellerFacilityBinController.class).activateBin(binId))
                .withRel("activate-bin");
    }

    public static Link deactivateBin(String binId) {
        return linkTo(methodOn(SellerFacilityBinController.class).deactivateBin(binId))
                .withRel("deactivate-bin");
    }

    public static Link deleteBin(String binId) {
        return linkTo(methodOn(SellerFacilityBinController.class).deleteBin(binId))
                .withRel("delete-bin");
    }

    public static Link createLocation() {
        return linkTo(methodOn(SellerFacilityLocationController.class).createLocation(null, null))
                .withRel("create-location");
    }
}
