package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.CategoryView;
import com.catalog.infrastructure.view.ProductHeroMediaView;
import com.catalog.infrastructure.view.ProductVariantRefView;
import com.catalog.infrastructure.view.ProductView;
import com.grab.framework.storage.FileStoragePort;
import com.grab.store.catalog.queries.SearchStorefrontProductsQuery;
import com.grab.store.catalog.queries.StorefrontProductSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchStorefrontProductsQueryHandlerTest {

    @Mock
    private ProductQueryRepository productQueryRepository;
    @Mock
    private CategoryQueryRepository categoryRepository;
    @Mock
    private FileStoragePort fileStoragePort;

    private SearchStorefrontProductsQueryHandler handler;

    @BeforeEach
    void setUp() {
        StorefrontProductSearchMapper mapper = new StorefrontProductSearchMapper(
                productQueryRepository, categoryRepository, fileStoragePort);
        handler = new SearchStorefrontProductsQueryHandler(mapper);
    }

    @Test
    void handle_requiresStorefrontVisibleAndMapsVariants() {
        ProductView view = new ProductView(
                "prod-1", "Shirt", "ACTIVE", "shirt", "cat-1", "merchant-1", true, "NEW");
        PageRequest pageable = PageRequest.of(0, 10);
        when(productQueryRepository.search(any(ProductSearchCriteria.class), any()))
                .thenReturn(new PageImpl<>(List.of(view), pageable, 1));
        when(categoryRepository.findViewByIds(List.of("cat-1")))
                .thenReturn(List.of(new CategoryView("cat-1", "Apparel", null, true, true, true)));
        when(productQueryRepository.findHeroMediasByProductIds(List.of("prod-1")))
                .thenReturn(List.of(new ProductHeroMediaView(
                        "prod-1", "media-1", "hero.jpg", "image/jpeg", 0)));
        when(productQueryRepository.findActiveVariantsByProductIds(List.of("prod-1")))
                .thenReturn(List.of(new ProductVariantRefView("prod-1", "var-1", "SKU-1")));
        when(fileStoragePort.resolvePublicUrl(anyString())).thenReturn("http://cdn.test/hero.jpg");

        Page<StorefrontProductSearchResult> page = handler.handle(new SearchStorefrontProductsQuery(
                "shirt", "cat-1", "NEW", true, pageable
        ));

        ArgumentCaptor<ProductSearchCriteria> captor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        verify(productQueryRepository).search(captor.capture(), any());
        assertThat(captor.getValue().storefrontVisible()).isTrue();
        assertThat(captor.getValue().featured()).isTrue();
        assertThat(captor.getValue().condition()).isEqualTo("NEW");

        StorefrontProductSearchResult result = page.getContent().getFirst();
        assertThat(result.productId()).isEqualTo("prod-1");
        assertThat(result.merchantId()).isEqualTo("merchant-1");
        assertThat(result.featured()).isTrue();
        assertThat(result.thumbnail().url()).isEqualTo("http://cdn.test/hero.jpg");
        assertThat(result.variants()).containsExactly(
                new StorefrontProductSearchResult.VariantRef("var-1", "SKU-1"));
    }
}
