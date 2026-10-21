package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapper;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductJpaRepository;
import com.grab.discovery.adapter.persistence.specification.jpa.DiscoveryProductSearchSpecification;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.support.PersistenceExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryProductQueryAdapterTest {

    private static final Id SALES_CHANNEL_ID = new CommonId("web-1");
    private static final Id PRODUCT_ID = new CommonId("prod-1");

    @Mock
    private DiscoveryProductJpaRepository products;
    @Mock
    private DiscoveryProductSearchSpecification searchSpecification;
    @Mock
    private DiscoveryProductEntityMapper mapper;

    private PersistenceExecutor executor;
    private DiscoveryProductQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        executor = new DiscoveryPersistenceExecutor();
        adapter = new DiscoveryProductQueryAdapter(products, searchSpecification, mapper, executor);
    }

    @Test
    void searchVisible_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        DiscoveryProductEntity entity = new DiscoveryProductEntity();
        entity.setSalesChannelId("web-1");
        entity.setProductId("prod-1");

        Specification<DiscoveryProductEntity> spec = (root, query, cb) -> cb.conjunction();
        when(searchSpecification.visibleForSalesChannel("web-1")).thenReturn(spec);
        when(products.findAll(eq(spec), eq(pageable))).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        DiscoveryProductView view = new DiscoveryProductView(
                SALES_CHANNEL_ID,
                PRODUCT_ID,
                null,
                "Title",
                "title",
                null,
                null,
                "ACTIVE",
                true,
                true,
                "MMK",
                null,
                null,
                List.of(),
                List.of()
        );
        when(mapper.toView(entity)).thenReturn(view);

        Page<DiscoveryProductView> result = adapter.searchVisible(SALES_CHANNEL_ID, pageable);

        assertThat(result.getContent()).containsExactly(view);
        verify(products).findAll(spec, pageable);
    }

    @Test
    void findVisibleBySlug_returnsMappedView() {
        DiscoveryProductEntity entity = new DiscoveryProductEntity();
        entity.setSalesChannelId("web-1");
        entity.setSlug("shirt");

        when(products.findBySalesChannelIdAndSlugAndPublishedTrueAndChannelEnabledTrueAndProductStatus(
                "web-1",
                "shirt",
                "ACTIVE"
        )).thenReturn(Optional.of(entity));

        DiscoveryProductView view = new DiscoveryProductView(
                SALES_CHANNEL_ID,
                PRODUCT_ID,
                null,
                "Shirt",
                "shirt",
                null,
                null,
                "ACTIVE",
                true,
                true,
                "MMK",
                null,
                null,
                List.of(),
                List.of()
        );
        when(mapper.toView(entity)).thenReturn(view);

        Optional<DiscoveryProductView> result = adapter.findVisibleBySlug(SALES_CHANNEL_ID, "shirt");

        assertThat(result).contains(view);
    }
}
