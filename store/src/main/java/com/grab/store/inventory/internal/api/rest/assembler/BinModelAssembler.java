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
        return EntityModel.of(bin,
                linkTo(methodOn(BinController.class)
                        .listBins(bin.zoneId(), null, null, null)).withRel("bins")
        );
    }
}
