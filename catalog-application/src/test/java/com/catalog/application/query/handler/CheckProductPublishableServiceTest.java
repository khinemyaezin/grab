package com.catalog.application.query.handler;

import com.catalog.application.service.CheckProductPublishableService;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.query.CheckProductPublishableQuery;
import com.catalog.application.query.CheckProductPublishableResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckProductPublishableServiceTest {

    @Mock
    private ProductRepository productRepository;

    private CheckProductPublishableService service;

    @BeforeEach
    void setUp() {
        service = new CheckProductPublishableService(productRepository, ids());
    }

    @Test
    void handle_whenOwnedAndActive_isPublishable() {
        when(productRepository.find(new CommonId("prod-1")))
                .thenReturn(Optional.of(product("merchant-1", ProductStatus.ACTIVE)));

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
        when(productRepository.find(new CommonId("prod-1")))
                .thenReturn(Optional.of(product("merchant-1", ProductStatus.DRAFT)));

        CheckProductPublishableResult result = service.execute(
                new CheckProductPublishableQuery("prod-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.active()).isFalse();
        assertThat(result.publishable()).isFalse();
    }

    @Test
    void handle_whenOwnedByAnotherMerchant_isNotPublishable() {
        when(productRepository.find(new CommonId("prod-1")))
                .thenReturn(Optional.of(product("merchant-other", ProductStatus.ACTIVE)));

        CheckProductPublishableResult result = service.execute(
                new CheckProductPublishableQuery("prod-1", "merchant-1")
        );

        assertThat(result.owned()).isFalse();
        assertThat(result.publishable()).isFalse();
    }

    @Test
    void handle_whenMissing_isNotPublishable() {
        when(productRepository.find(new CommonId("missing"))).thenReturn(Optional.empty());

        CheckProductPublishableResult result = service.execute(
                new CheckProductPublishableQuery("missing", "merchant-1")
        );

        assertThat(result).isEqualTo(CheckProductPublishableResult.missing());
    }

    private Product product(String merchantId, ProductStatus status) {
        return new Product(
                new CommonId("prod-1"),
                new CommonId(merchantId),
                "Shirt",
                new CommonId("cat-1"),
                null,
                status,
                "shirt",
                List.of(),
                List.of(),
                List.of()
        );
    }

    private IdGenerator ids() {
        return new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("new");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
    }
}
