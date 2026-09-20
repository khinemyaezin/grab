package com.catalog.application.query.handler;

import com.catalog.application.service.ProductSearchService;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.readmodel.ProductSearchCriteria;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.readmodel.ProductHeroMediaView;
import com.catalog.application.readmodel.ProductPublicationView;
import com.catalog.application.readmodel.ProductView;
import com.grab.framework.storage.FileStoragePort;
import com.catalog.application.query.ProductSearchQuery;
import com.catalog.application.query.ProductSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

    @Mock
    private ProductQueryPort productQueryRepository;
    @Mock
    private CategoryQueryPort categoryRepository;
    @Mock
    private FileStoragePort fileStoragePort;

    private ProductSearchService service;

    @BeforeEach
    void setUp() {
        service = new ProductSearchService(productQueryRepository, categoryRepository, fileStoragePort);
    }

    @Test
    void handle_resolvesHeroThumbnailUrl_andLeavesMissingMediaNull() {
        ProductView withHero = new ProductView("prod-1", "Shirt", "ACTIVE", "shirt", "cat-1");
        ProductView withoutMedia = new ProductView("prod-2", "Hat", "DRAFT", "hat", "cat-1");
        PageRequest pageable = PageRequest.of(0, 10);
        when(productQueryRepository.search(any(ProductSearchCriteria.class), any()))
                .thenReturn(new PageImpl<>(List.of(withHero, withoutMedia), pageable, 2));
        when(categoryRepository.findViewByIds(List.of("cat-1")))
                .thenReturn(List.of(new CategoryView("cat-1", "Apparel", null, true, true, true)));
        when(productQueryRepository.findHeroMediasByProductIds(List.of("prod-1", "prod-2")))
                .thenReturn(List.of(new ProductHeroMediaView(
                        "prod-1",
                        "media-1",
                        "merchants/m/products/prod-1/hero.jpg",
                        "image/jpeg",
                        0
                )));
        when(fileStoragePort.resolvePublicUrl(anyString())).thenAnswer(invocation ->
                "http://cdn.test/" + invocation.getArgument(0));

        Page<ProductSearchResult> page = service.execute(new ProductSearchQuery(
                "merchant-1", null, null, null, null, pageable
        ));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().getFirst().thumbnail()).isEqualTo(new ProductSearchResult.Media(
                "media-1",
                "merchants/m/products/prod-1/hero.jpg",
                "http://cdn.test/merchants/m/products/prod-1/hero.jpg",
                "image/jpeg",
                0
        ));
        assertThat(page.getContent().get(1).thumbnail()).isNull();

        verify(productQueryRepository).findHeroMediasByProductIds(eq(List.of("prod-1", "prod-2")));
    }

    @Test
    void handle_deduplicatesProductLevelSalesChannelIds() {
        ProductView product = new ProductView("prod-1", "Shirt", "ACTIVE", "shirt", "cat-1");
        PageRequest pageable = PageRequest.of(0, 10);
        when(productQueryRepository.search(any(ProductSearchCriteria.class), any()))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));
        when(categoryRepository.findViewByIds(List.of("cat-1")))
                .thenReturn(List.of(new CategoryView("cat-1", "Apparel", null, true, true, true)));
        when(productQueryRepository.findHeroMediasByProductIds(List.of("prod-1")))
                .thenReturn(List.of());
        when(productQueryRepository.findPublicationsByProductIds(List.of("prod-1")))
                .thenReturn(List.of(
                        new ProductPublicationView("prod-1", "var-1", "channel-1"),
                        new ProductPublicationView("prod-1", "var-2", "channel-1"),
                        new ProductPublicationView("prod-1", "var-2", "channel-2")
                ));

        Page<ProductSearchResult> page = service.execute(new ProductSearchQuery(
                "merchant-1", null, null, null, null, pageable
        ));

        assertThat(page.getContent().getFirst().publications()).containsExactly(
                new ProductSearchResult.Publication("channel-1"),
                new ProductSearchResult.Publication("channel-2")
        );
    }
}
