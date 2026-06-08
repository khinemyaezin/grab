package com.grab.store.inventory;

import com.grab.store.shared.LinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(Link.of("/api/v1/inventory").withSelfRel());
        model.add(Link.of("/api/v1/inventory/items").withRel(LinkRelations.INVENTORIES));
        model.add(Link.of("/api/v1/inventory/locations").withRel(LinkRelations.LOCATIONS));
        model.add(Link.of("/api/v1/inventory/zones").withRel(LinkRelations.ZONES));
        model.add(Link.of("/api/v1/inventory/bins").withRel(LinkRelations.BINS));
        return ResponseEntity.ok(model);
    }
}
