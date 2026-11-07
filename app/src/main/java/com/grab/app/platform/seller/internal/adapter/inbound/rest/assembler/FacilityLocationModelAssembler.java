package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerFacilityLocationController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FacilityLocationModelAssembler implements RepresentationModelAssembler<FacilityLocationResponse, EntityModel<FacilityLocationResponse>> {

    @Override
    public EntityModel<FacilityLocationResponse> toModel(FacilityLocationResponse location) {
        if (location == null) {
            return null;
        }
        EntityModel<FacilityLocationResponse> model = EntityModel.of(location);
        if (location.id() != null) {
            model.add(linkTo(methodOn(SellerFacilityLocationController.class).getLocation(location.id())).withSelfRel());
            model.add(linkTo(methodOn(SellerFacilityLocationController.class).activateLocation(location.id())).withRel("activate-location"));
            model.add(linkTo(methodOn(SellerFacilityLocationController.class).deactivateLocation(location.id())).withRel("deactivate-location"));
            model.add(linkTo(methodOn(SellerFacilityLocationController.class).deleteLocation(location.id())).withRel("delete-location"));
        }
        return model;
    }
}
