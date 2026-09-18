package com.grab.store.saleschannel;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class SalesChannelRootControllerTest {

    @Test
    void root_shouldExposeListAndGetLinks() {
        ResponseEntity<RepresentationModel<?>> response = new SalesChannelRootController().root();
        RepresentationModel<?> body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getLink("list-sales-channels")).isPresent();
        assertThat(body.getLink("get-sales-channel")).isPresent();
        assertThat(body.getRequiredLink("list-sales-channels").getHref())
                .contains("/api/v1/sales-channels/channels");
    }
}
