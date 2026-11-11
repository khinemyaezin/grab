package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/inventory")
public class SellerInventoryRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerInventoryRootController.class).root()).withSelfRel());

        model.add(linkTo(methodOn(SellerFacilityLocationController.class).createLocation(null, null, null))
                .withRel("create-location"));
        model.add(linkTo(methodOn(SellerFacilityLocationController.class).getLocation(null))
                .withRel("location"));

        model.add(linkTo(methodOn(SellerInventoryController.class).createItem(null, null, null))
                .withRel("create-inventory-item"));
        model.add(linkTo(methodOn(SellerInventoryController.class).getItem(null))
                .withRel("inventory-item"));
        model.add(linkTo(methodOn(SellerInventoryController.class).getLatestMovement(null))
                .withRel("latest-movement"));

        model.add(linkTo(methodOn(SellerAllocationController.class).allocateStock(null, null))
                .withRel("allocate-stock"));
        model.add(linkTo(methodOn(SellerAllocationController.class).deallocateStock(null, null))
                .withRel("deallocate-stock"));

        model.add(linkTo(methodOn(SellerReorderSuggestionController.class).listSuggestions(null, null, null))
                .withRel("reorder-suggestions"));

        return ResponseEntity.ok(model);
    }
}
