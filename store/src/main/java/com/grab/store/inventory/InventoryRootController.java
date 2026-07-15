package com.grab.store.inventory;

import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.controller.LocationController;
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
public class InventoryRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();

        model.add(linkTo(methodOn(InventoryRootController.class).root())
                .withSelfRel());

        model.add(linkTo(methodOn(LocationController.class)
                .searchLocations(null, null, null, null))
                .withRel("search-locations"));
        model.add(linkTo(methodOn(LocationController.class)
                .createLocation(null, null))
                .withRel("create-location"));
        model.add(linkTo(methodOn(LocationController.class)
                .getLocation(null))
                .withRel("location"));

        model.add(linkTo(methodOn(InventoryController.class)
                .searchInventoryItems(null, null, null, null))
                .withRel("search-inventory-items"));
        model.add(linkTo(methodOn(InventoryController.class)
                .createInventory(null, null))
                .withRel("create-inventory-item"));
        model.add(linkTo(methodOn(InventoryController.class)
                .getInventory(null))
                .withRel("inventory-item"));

        return ResponseEntity.ok(model);
    }
}
