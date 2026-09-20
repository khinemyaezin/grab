package com.catalog.application.query.handler;

import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.model.read.CheckProductPublishableQuery;
import com.catalog.application.model.read.CheckProductPublishableResult;
import com.catalog.application.model.read.ProductView;
import com.catalog.application.service.CheckProductPublishableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckProductPublishableServiceTest {

    @Mock
    private ProductQueryPort productQueryPort;

    private CheckProductPublishableService service;

    @BeforeEach
    void setUp() {
        service = new CheckProductPublishableService(productQueryPort);
    }

    @Test
    void handle_whenOwnedAndActive_isPublishable() {
        when(productQueryPort.findByIdAndMerchantId("prod-1", "merchant-1"))
                .thenReturn(Optional.of(new ProductView("prod-1", "Shirt", "ACTIVE", "shirt", "cat-1", "merchant-1", false, null)));

        CheckProductPublishableResult result = service.execute(
                new CheckProductPublishableQuery("prod-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.owned()).isTrue();
        assertThat(result.active()).isTrue();
        assertThat(result.publishable()).isTrue();
    }

    @Test
    void handle_whenDraft_isNotPublishable() {
        when(productQueryPort.findByIdAndMerchantId("prod-1", "merchant-1"))
                .thenReturn(Optional.of(new ProductView("prod-1", "Shirt", "DRAFT", "shirt", "cat-1", "merchant-1", false, null)));

        CheckProductPublishableResult result = service.execute(
                new CheckProductPublishableQuery("prod-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.active()).isFalse();
        assertThat(result.publishable()).isFalse();
    }

    @Test
    void handle_whenOwnedByAnotherMerchant_isNotPublishable() {
        when(productQueryPort.findByIdAndMerchantId("prod-1", "merchant-1"))
                .thenReturn(Optional.of(new ProductView("prod-1", "Shirt", "ACTIVE", "shirt", "cat-1", "merchant-other", false, null)));

        CheckProductPublishableResult result = service.execute(
                new CheckProductPublishableQuery("prod-1", "merchant-1")
        );

        assertThat(result.owned()).isFalse();
        assertThat(result.publishable()).isFalse();
    }

    @Test
    void handle_whenMissing_isNotPublishable() {
        when(productQueryPort.findByIdAndMerchantId("missing", "merchant-1")).thenReturn(Optional.empty());

        CheckProductPublishableResult result = service.execute(
                new CheckProductPublishableQuery("missing", "merchant-1")
        );

        assertThat(result).isEqualTo(CheckProductPublishableResult.missing());
    }
}
