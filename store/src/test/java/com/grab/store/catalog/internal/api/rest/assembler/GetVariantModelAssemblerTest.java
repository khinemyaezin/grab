package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.GetVariantResponse;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetVariantModelAssemblerTest {

    private final GetVariantModelAssembler assembler = new GetVariantModelAssembler();

    @Test
    void toModel_shouldExposeBatchVariantImagesLinkForActiveVariant() {
        EntityModel<GetVariantResponse> model = assembler.toModel(variant("ACTIVE"));

        assertThat(model.getLink("batch-variant-images")).isPresent();
        assertThat(model.getRequiredLink("batch-variant-images").getHref())
                .contains("/products/prod-1/variants/var-1/images/batch");
        assertThat(model.getLink("update-variant")).isPresent();
    }

    @Test
    void toModel_shouldOmitBatchVariantImagesLinkForDeletedVariant() {
        EntityModel<GetVariantResponse> model = assembler.toModel(variant("DELETED"));

        assertThat(model.getLink("batch-variant-images")).isEmpty();
        assertThat(model.getLink("restore-variant")).isPresent();
    }

    private GetVariantResponse variant(String status) {
        return new GetVariantResponse(
                "prod-1",
                "Shirt",
                "var-1",
                "TSHIRT-RED-L",
                status,
                "red-l",
                List.of(),
                true,
                List.of("media-1"),
                "media-1"
        );
    }
}
