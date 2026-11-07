package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerFacilityZoneController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FacilityZoneModelAssembler implements RepresentationModelAssembler<FacilityZoneResponse, EntityModel<FacilityZoneResponse>> {

    @Override
    public EntityModel<FacilityZoneResponse> toModel(FacilityZoneResponse zone) {
        if (zone == null) {
            return null;
        }
        EntityModel<FacilityZoneResponse> model = EntityModel.of(zone);
        if (zone.id() != null) {
            model.add(linkTo(methodOn(SellerFacilityZoneController.class).getZone(zone.id())).withSelfRel());
            model.add(linkTo(methodOn(SellerFacilityZoneController.class).activateZone(zone.id())).withRel("activate-zone"));
            model.add(linkTo(methodOn(SellerFacilityZoneController.class).deactivateZone(zone.id())).withRel("deactivate-zone"));
            model.add(linkTo(methodOn(SellerFacilityZoneController.class).deleteZone(zone.id())).withRel("delete-zone"));
        }
        return model;
    }
}
