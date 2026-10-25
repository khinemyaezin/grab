package com.grab.app.productmerchandising.catalog.internal.api.rest.assembler;

import com.grab.app.productmerchandising.catalog.api.representation.GetVariantResponse;
import com.grab.app.ordermanagement.workflows.internal.api.rest.processor.GetVariantWorkflowLinksProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetVariantModelAssemblerTest {

    private final GetVariantModelAssembler assembler = new GetVariantModelAssembler();

    @Test
    void toModel_shouldExposeBatchVariantImagesLinkForActiveVariant() {
        EntityModel<GetVariantResponse> model = processedModel(variant("ACTIVE"));

        assertThat(model.getLink("batch-variant-images")).isPresent();
        assertThat(model.getRequiredLink("batch-variant-images").getHref())
                .contains("/products/prod-1/variants/var-1/images/batch");
        assertThat(model.getLink("update-variant")).isPresent();
        assertThat(model.getLink("update-product-variant")).isPresent();
        assertThat(model.getLinks("update-product-variant")).hasSize(1);
    }

    @Test
    void toModel_shouldOmitBatchVariantImagesLinkForDeletedVariant() {
        EntityModel<GetVariantResponse> model = processedModel(variant("DELETED"));

        assertThat(model.getLink("batch-variant-images")).isEmpty();
        assertThat(model.getLink("restore-variant")).isPresent();
        assertThat(model.getLink("update-product-variant")).isEmpty();
    }

    @Test
    void toModel_shouldExposeWorkflowLinkForArchivedVariant() {
        EntityModel<GetVariantResponse> model = processedModel(variant("ARCHIVED"));

        assertThat(model.getLink("update-product-variant")).isPresent();
        assertThat(model.getLinks("update-product-variant")).hasSize(1);
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

    private EntityModel<GetVariantResponse> processedModel(GetVariantResponse response) {
        return new GetVariantWorkflowLinksProcessor().process(assembler.toModel(response));
    }
}
