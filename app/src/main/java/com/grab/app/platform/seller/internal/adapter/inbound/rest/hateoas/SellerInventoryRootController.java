package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class SellerInventoryRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.inventoryRoot().withSelfRel());

        model.add(SellerFacilityApiLinks.createLocation());
        model.add(SellerFacilityApiLinks.getLocation(null));

        model.add(SellerInventoryApiLinks.createItem());
        model.add(SellerInventoryApiLinks.getItem(null));
        model.add(SellerInventoryApiLinks.getLatestMovement(null));

        model.add(SellerAllocationApiLinks.allocateStock());
        model.add(SellerAllocationApiLinks.deallocateStock());

        model.add(SellerReorderSuggestionApiLinks.listSuggestions(null, null));

        return ResponseEntity.ok(model);
    }
}
