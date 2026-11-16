package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerFacilityApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class FacilityBinModelAssembler implements RepresentationModelAssembler<FacilityBinResponse, EntityModel<FacilityBinResponse>> {

    @Override
    public EntityModel<FacilityBinResponse> toModel(FacilityBinResponse bin) {
        return assemble(bin, true);
    }

    public EntityModel<FacilityBinResponse> toCommandModel(FacilityBinResponse bin) {
        return assemble(bin, false);
    }

    private EntityModel<FacilityBinResponse> assemble(FacilityBinResponse bin, boolean canonicalRead) {
        if (bin == null) {
            return null;
        }
        EntityModel<FacilityBinResponse> model = EntityModel.of(bin);
        if (bin.id() != null) {
            Link retrieval = SellerFacilityApiLinks.getBin(bin.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
            model.add(SellerFacilityApiLinks.activateBin(bin.id()));
            model.add(SellerFacilityApiLinks.deactivateBin(bin.id()));
            model.add(SellerFacilityApiLinks.deleteBin(bin.id()));
        }
        return model;
    }
}
