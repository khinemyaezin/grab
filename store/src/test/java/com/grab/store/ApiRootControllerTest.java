package com.grab.store;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApiRootControllerTest {

    @Test
    void root_shouldContainEventStreamLink() {
        ApiRootController controller = new ApiRootController();
        ResponseEntity<RepresentationModel<?>> response = controller.root();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLink("event-stream")).isPresent();
        assertThat(response.getBody().getLink("get-storefront-root")).isPresent();
        assertThat(response.getBody().getLink("get-storefront-root").orElseThrow().getHref())
                .endsWith("/api/v1/storefront");
        assertThat(response.getBody().getLink("event-stream").orElseThrow().getHref())
                .endsWith("/api/v1/events/stream");
    }
}
