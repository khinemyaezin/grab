package com.grab.app.productmerchandising.api;

import org.springframework.hateoas.Link;
import com.grab.app.productmerchandising.catalog.CatalogRootController;
import com.grab.app.productmerchandising.inventory.InventoryRootController;
import com.grab.app.productmerchandising.pricing.PricingRootController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class ProductMerchandisingApiLinks {
    private ProductMerchandisingApiLinks() {}

    public static Link catalogRoot() {
        return linkTo(methodOn(CatalogRootController.class).root()).withRel("get-catalog-root");
    }
    public static Link inventoryRoot() {
        return linkTo(methodOn(InventoryRootController.class).root()).withRel("get-inventory-root");
    }
    public static Link pricingRoot() {
        return linkTo(methodOn(PricingRootController.class).root()).withRel("get-pricing-root");
    }
}
