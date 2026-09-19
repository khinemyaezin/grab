package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.ProductPublicationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPublicationModelAssemblerTest {

    private final ProductPublicationModelAssembler assembler = new ProductPublicationModelAssembler();

    @Test
    void toModel_shouldExposeUnpublishAndGetProductLinks() {
        EntityModel<ProductPublicationResponse> model = assembler.toModel(new ProductPublicationResponse(
                "prod-1",
                List.of(new ProductPublicationResponse.Publication("var-1", "channel-1"))
        ));

        assertThat(model.getLink("get-product")).isPresent();
        assertThat(model.getLink("unpublish-product-from-channel")).isPresent();
        assertThat(model.getLink("update-sellable-product")).isPresent();
        assertThat(model.getRequiredLink("unpublish-product-from-channel").getHref())
                .contains("/products/prod-1/channels/unpublish");
        assertThat(model.getRequiredLink("update-sellable-product").getHref())
                .contains("/workflows/update-sellable-product");
    }
}
