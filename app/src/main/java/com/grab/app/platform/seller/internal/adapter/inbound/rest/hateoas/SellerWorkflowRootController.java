package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflows/root")
public class SellerWorkflowRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.workflowRoot().withSelfRel());

        model.add(SellerWorkflowApiLinks.startCreateSellableProduct());
        model.add(SellerWorkflowApiLinks.getCreateSellableProduct(null));

        model.add(SellerWorkflowApiLinks.startUpdateSellableProduct());
        model.add(SellerWorkflowApiLinks.getUpdateSellableProduct(null));

        model.add(SellerWorkflowApiLinks.startUpdateProductVariant());
        model.add(SellerWorkflowApiLinks.getUpdateProductVariant(null));

        return ResponseEntity.ok(model);
    }
}

