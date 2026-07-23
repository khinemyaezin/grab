package com.grab.store;

import com.grab.store.catalog.CatalogRootController;
import com.grab.store.inventory.InventoryRootController;
import com.grab.store.identity.IdentityRootController;
import com.grab.store.merchant.MerchantRootController;
import com.grab.store.workflows.WorkflowsRootController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1")
public class ApiRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(ApiRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(CatalogRootController.class).root()).withRel("get-catalog-root"));
        model.add(linkTo(methodOn(InventoryRootController.class).root()).withRel("get-inventory-root"));
        model.add(linkTo(methodOn(IdentityRootController.class).root()).withRel("get-identity-root"));
        model.add(linkTo(methodOn(MerchantRootController.class).root()).withRel("get-merchant-root"));
        model.add(linkTo(methodOn(WorkflowsRootController.class).root()).withRel("get-workflows-root"));
        return ResponseEntity.ok(model);
    }
}
