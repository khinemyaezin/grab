package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerFacilityBinController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FacilityBinModelAssembler implements RepresentationModelAssembler<FacilityBinResponse, EntityModel<FacilityBinResponse>> {

    @Override
    public EntityModel<FacilityBinResponse> toModel(FacilityBinResponse bin) {
        if (bin == null) {
            return null;
        }
        EntityModel<FacilityBinResponse> model = EntityModel.of(bin);
        if (bin.id() != null) {
            model.add(linkTo(methodOn(SellerFacilityBinController.class).getBin(bin.id())).withSelfRel());
            model.add(linkTo(methodOn(SellerFacilityBinController.class).activateBin(bin.id())).withRel("activate-bin"));
            model.add(linkTo(methodOn(SellerFacilityBinController.class).deactivateBin(bin.id())).withRel("deactivate-bin"));
            model.add(linkTo(methodOn(SellerFacilityBinController.class).deleteBin(bin.id())).withRel("delete-bin"));
        }
        return model;
    }
}
