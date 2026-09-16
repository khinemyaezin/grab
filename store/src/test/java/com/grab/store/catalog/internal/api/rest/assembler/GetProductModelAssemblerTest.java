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
    }

    private GetProductResponse product(String status) {
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
                List.of(),
                List.of()
        );
    }
}
