package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.BinController;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BinModelAssembler implements RepresentationModelAssembler<BinResponse, EntityModel<BinResponse>> {

    @Override
    public EntityModel<BinResponse> toModel(BinResponse bin) {
        EntityModel<BinResponse> entityModel = EntityModel.of(bin,
                linkTo(methodOn(BinController.class)
                        .getBinById(bin.id())).withSelfRel(),

                linkTo(methodOn(BinController.class)
                        .listBins(bin.zoneId(), null, null, null))
                        .withRel("paged-bins")
        );
        entityModel.add(linkTo(methodOn(BinController.class)
                .updateBin(bin.id(), null, null))
                .withRel("edit-bin"));

        entityModel.add(linkTo(methodOn(BinController.class)
                .deleteBin(bin.id(), null))
                .withRel("delete-bin"));

        if (bin.active()) {
            entityModel.add(linkTo(methodOn(BinController.class)
                    .deactivateBin(bin.id(), null))
                    .withRel("deactivate-bin"));
        } else {
            entityModel.add(linkTo(methodOn(BinController.class)
                    .activateBin(bin.id(), null))
                    .withRel("activate-bin"));
        }

        return entityModel;
    }
}
