package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class SellerCatalogRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.catalogRoot().withSelfRel());

        model.add(SellerProductApiLinks.listProducts());
        model.add(SellerProductApiLinks.searchProducts(null, 0, 20));
        model.add(SellerProductApiLinks.getProduct(null));
        model.add(SellerProductApiLinks.getVariant(null, null));
        model.add(SellerProductApiLinks.createProduct());

        model.add(SellerCategoryApiLinks.getLeafNodes(null));
        model.add(SellerCategoryApiLinks.getCategoryTree(null));

        model.add(SellerVariantOptionApiLinks.listVariantOptions(null, null));
        model.add(SellerVariantOptionApiLinks.listVariantTypes(null));

        model.add(SellerWorkflowApiLinks.startCreateSellableProduct());
        model.add(SellerWorkflowApiLinks.startUpdateSellableProduct());
        model.add(SellerWorkflowApiLinks.startUpdateProductVariant());

        return ResponseEntity.ok(model);
    }
}
