package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerFacilityApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class FacilityLocationModelAssembler implements RepresentationModelAssembler<FacilityLocationResponse, EntityModel<FacilityLocationResponse>> {

    @Override
    public EntityModel<FacilityLocationResponse> toModel(FacilityLocationResponse location) {
        return assemble(location, true);
    }

    public EntityModel<FacilityLocationResponse> toCommandModel(FacilityLocationResponse location) {
        return assemble(location, false);
    }

    private EntityModel<FacilityLocationResponse> assemble(FacilityLocationResponse location, boolean canonicalRead) {
        if (location == null) {
            return null;
        }
        EntityModel<FacilityLocationResponse> model = EntityModel.of(location);
        if (location.id() != null) {
            Link retrieval = SellerFacilityApiLinks.getLocation(location.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
            model.add(SellerFacilityApiLinks.activateLocation(location.id()));
            model.add(SellerFacilityApiLinks.deactivateLocation(location.id()));
            model.add(SellerFacilityApiLinks.deleteLocation(location.id()));
        }
        return model;
    }
}
