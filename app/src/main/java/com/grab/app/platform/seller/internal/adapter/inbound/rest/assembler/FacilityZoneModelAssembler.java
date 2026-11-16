package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerFacilityApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class FacilityZoneModelAssembler implements RepresentationModelAssembler<FacilityZoneResponse, EntityModel<FacilityZoneResponse>> {

    @Override
    public EntityModel<FacilityZoneResponse> toModel(FacilityZoneResponse zone) {
        return assemble(zone, true);
    }

    public EntityModel<FacilityZoneResponse> toCommandModel(FacilityZoneResponse zone) {
        return assemble(zone, false);
    }

    private EntityModel<FacilityZoneResponse> assemble(FacilityZoneResponse zone, boolean canonicalRead) {
        if (zone == null) {
            return null;
        }
        EntityModel<FacilityZoneResponse> model = EntityModel.of(zone);
        if (zone.id() != null) {
            Link retrieval = SellerFacilityApiLinks.getZone(zone.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
            model.add(SellerFacilityApiLinks.activateZone(zone.id()));
            model.add(SellerFacilityApiLinks.deactivateZone(zone.id()));
            model.add(SellerFacilityApiLinks.deleteZone(zone.id()));
        }
        return model;
    }
}
