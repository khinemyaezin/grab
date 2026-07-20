package com.grab.store.inventory;

import com.grab.store.catalog.api.CatalogApiLinks;
import com.grab.store.inventory.internal.api.rest.controller.AllocationController;
import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.controller.LocationController;
import com.grab.store.inventory.internal.api.rest.controller.ReorderSuggestionController;
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
                .checkExistence(null, null))
                .withRel("check-inventory-items-existence"));
        model.add(linkTo(methodOn(InventoryController.class)
                .getInventorySummary(null, null))
                .withRel("inventory-summary"));
        model.add(linkTo(methodOn(InventoryController.class)
                .getInventory(null))
                .withRel("inventory-item"));

        model.add(linkTo(methodOn(AllocationController.class)
                .allocate(null, null))
                .withRel("allocate-stock"));
        model.add(linkTo(methodOn(AllocationController.class)
                .deallocate(null, null))
                .withRel("deallocate-stock"));
        model.add(linkTo(methodOn(AllocationController.class)
                .availability(null, null))
                .withRel("allocation-availability"));
        model.add(linkTo(methodOn(ReorderSuggestionController.class)
                .list(null, null, null))
                .withRel("reorder-suggestions"));

        model.add(CatalogApiLinks.searchProductVariants());
        return ResponseEntity.ok(model);
    }
}
