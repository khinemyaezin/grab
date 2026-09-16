package com.grab.store.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogRootControllerTest {

    private final CatalogRootController controller = new CatalogRootController();

    @Test
    void root_shouldExposeTemplatedMediaLinks() {
        ResponseEntity<RepresentationModel<?>> response = controller.root();
        RepresentationModel<?> body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getLink("create-staged-media-upload")).isPresent();
        assertThat(body.getLink("create-product-media-upload")).isPresent();
        assertThat(body.getLink("replace-product-media")).isPresent();
        assertThat(body.getLink("replace-product-descriptions")).isPresent();
        assertThat(body.getLink("batch-variant-images")).isPresent();
        assertThat(body.getRequiredLink("create-staged-media-upload").isTemplated()).isFalse();
        assertThat(body.getRequiredLink("create-product-media-upload").isTemplated()).isTrue();
        assertThat(body.getRequiredLink("replace-product-media").isTemplated()).isTrue();
        assertThat(body.getRequiredLink("replace-product-descriptions").isTemplated()).isTrue();
        assertThat(body.getRequiredLink("batch-variant-images").isTemplated()).isTrue();
        assertThat(body.getRequiredLink("create-staged-media-upload").getHref())
                .contains("/catalog/media/uploads");
        assertThat(body.getRequiredLink("replace-product-descriptions").getHref())
                .contains("/products/{productId}/descriptions");
        assertThat(body.getRequiredLink("batch-variant-images").getHref())
                .contains("/products/{productId}/variants/{variantId}/images/batch");
    }
}
