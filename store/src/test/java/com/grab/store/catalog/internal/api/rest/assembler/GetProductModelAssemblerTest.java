package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.GetProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetProductModelAssemblerTest {

    private final GetProductModelAssembler assembler = new GetProductModelAssembler();

    @Test
    void toModel_shouldExposeMediaUploadAndReplaceLinks() {
        EntityModel<GetProductResponse> model = assembler.toModel(product("DRAFT"));

        assertThat(model.getLink("create-product-media-upload")).isPresent();
        assertThat(model.getLink("replace-product-media")).isPresent();
        assertThat(model.getLink("replace-product-descriptions")).isPresent();
        assertThat(model.getRequiredLink("create-product-media-upload").getHref())
                .contains("/products/prod-1/media/uploads");
        assertThat(model.getRequiredLink("replace-product-media").getHref())
                .contains("/products/prod-1/media");
        assertThat(model.getRequiredLink("replace-product-descriptions").getHref())
                .contains("/products/prod-1/descriptions");
        assertThat(model.getLink("update-sellable-product")).isPresent();
        assertThat(model.getRequiredLink("update-sellable-product").getHref())
                .contains("/workflows/update-sellable-product");
    }

    @Test
    void toModel_whenActive_shouldExposeUpdateSellableProductLink() {
        EntityModel<GetProductResponse> model = assembler.toModel(product("ACTIVE", List.of()));

        assertThat(model.getLink("publish-product")).isEmpty();
        assertThat(model.getLink("publish-product-to-channel")).isEmpty();
        assertThat(model.getLink("update-sellable-product")).isPresent();
        assertThat(model.getLink("unpublish-product-from-channel")).isEmpty();
        assertThat(model.getRequiredLink("update-sellable-product").getHref())
                .contains("/workflows/update-sellable-product");
    }

    @Test
    void toModel_whenPublished_shouldExposeUnpublishLink() {
        EntityModel<GetProductResponse> model = assembler.toModel(product(
                "ACTIVE",
                List.of(new GetProductResponse.Publication("channel-1"))
        ));

        assertThat(model.getLink("unpublish-product-from-channel")).isPresent();
        assertThat(model.getRequiredLink("unpublish-product-from-channel").getHref())
                .contains("/products/prod-1/channels/unpublish");
    }

    private GetProductResponse product(String status) {
        return product(status, List.of());
    }

    private GetProductResponse product(String status, List<GetProductResponse.Publication> publications) {
        return new GetProductResponse(
                "prod-1",
                "Shirt",
                new GetProductResponse.Category("cat-1", "Apparel"),
                "seller-1",
                "FIRST_PARTY_RETAILER",
                "NEW",
                true,
                status,
                "shirt",
                false,
                List.of(),
                List.of(),
                null,
                List.of(new GetProductResponse.Variant(
                        "var-1",
                        "SKU-1",
                        "ACTIVE",
                        "key",
                        List.of(),
                        false,
                        List.of(),
                        null,
                        publications
                )),
                List.of()
        );
    }
}
