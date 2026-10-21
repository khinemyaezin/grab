package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryProductEntityMapper;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryProductJpaRepository;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.support.PersistenceExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryProductRepositoryAdapterTest {

    private static final Id SALES_CHANNEL_ID = new CommonId("web-1");
    private static final Id PRODUCT_ID = new CommonId("prod-1");
    private static final Id VARIANT_ID = new CommonId("var-1");

    @Mock
    private DiscoveryProductJpaRepository products;
    @Mock
    private DiscoveryProductEntityMapper mapper;

    private PersistenceExecutor executor;
    private DiscoveryProductRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        executor = new DiscoveryPersistenceExecutor();
        adapter = new DiscoveryProductRepositoryAdapter(products, mapper, executor);
    }

    @Test
    void findByVariant_usesTargetedQuery() {
        DiscoveryProductEntity entity = new DiscoveryProductEntity();
        when(products.findByVariantIdInVariants("var-1")).thenReturn(List.of(entity));

        DiscoveryProductProjection projection = new DiscoveryProductProjection(
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
                true,
                "MMK",
                null,
                null,
                List.of(),
                List.of(),
                Instant.now()
        );
        when(mapper.toProjection(entity)).thenReturn(projection);

        List<DiscoveryProductProjection> results = adapter.findByVariant(VARIANT_ID);

        assertThat(results).containsExactly(projection);
        verify(products).findByVariantIdInVariants("var-1");
    }

    @Test
    void findBySku_usesTargetedQuery() {
        DiscoveryProductEntity entity = new DiscoveryProductEntity();
        when(products.findBySkuInVariants("SKU-123")).thenReturn(List.of(entity));

        DiscoveryProductProjection projection = new DiscoveryProductProjection(
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
                true,
                "MMK",
                null,
                null,
                List.of(),
                List.of(),
                Instant.now()
        );
        when(mapper.toProjection(entity)).thenReturn(projection);

        List<DiscoveryProductProjection> results = adapter.findBySku("SKU-123");

        assertThat(results).containsExactly(projection);
        verify(products).findBySkuInVariants("SKU-123");
    }

    @Test
    void save_updatesDocumentAndSaves() {
        DiscoveryProductProjection projection = new DiscoveryProductProjection(
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
                true,
                "MMK",
                null,
                null,
                List.of(),
                List.of(),
                Instant.now()
        );

        when(products.findBySalesChannelIdAndProductId("web-1", "prod-1")).thenReturn(Optional.empty());

        adapter.save(projection);

        verify(mapper).updateEntity(eq(projection), any(DiscoveryProductEntity.class));
        verify(products).save(any(DiscoveryProductEntity.class));
    }
}
