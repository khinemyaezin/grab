package com.grab.store.workflows;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowsRootControllerTest {

    @Test
    void root_shouldExposePublishProductToChannelLinks() {
        ResponseEntity<RepresentationModel<?>> response = new WorkflowsRootController().root();
        RepresentationModel<?> body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getLink("publish-product-to-channel")).isPresent();
        assertThat(body.getLink("get-publish-product-to-channel")).isPresent();
        assertThat(body.getRequiredLink("publish-product-to-channel").getHref())
                .contains("/api/v1/workflows/publish-product-to-channel");
    }
}
