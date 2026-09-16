package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.CategoryView;
import com.catalog.infrastructure.view.ProductHeroMediaView;
import com.catalog.infrastructure.view.ProductView;
import com.grab.framework.storage.FileStoragePort;
import com.grab.store.catalog.internal.query.ProductSearchQuery;
import com.grab.store.catalog.internal.query.ProductSearchResult;
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
class ProductSearchQueryHandlerTest {

    @Mock
    private ProductQueryRepository productQueryRepository;
    @Mock
    private CategoryQueryRepository categoryRepository;
    @Mock
    private FileStoragePort fileStoragePort;

    private ProductSearchQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProductSearchQueryHandler(productQueryRepository, categoryRepository, fileStoragePort);
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

        Page<ProductSearchResult> page = handler.handle(new ProductSearchQuery(
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
}
