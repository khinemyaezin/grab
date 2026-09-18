package com.grab.store.workflows;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowsRootControllerTest {

    @Test
    void root_shouldExposeUpdateSellableProductLinks() {
        ResponseEntity<RepresentationModel<?>> response = new WorkflowsRootController().root();
        RepresentationModel<?> body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getLink("update-sellable-product")).isPresent();
        assertThat(body.getLink("get-update-sellable-product")).isPresent();
        assertThat(body.getRequiredLink("update-sellable-product").getHref())
                .contains("/api/v1/workflows/update-sellable-product");
        assertThat(body.getLink("publish-product-to-channel")).isEmpty();
        assertThat(body.getLink("get-publish-product-to-channel")).isEmpty();
    }
}
