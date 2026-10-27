package com.grab.app.catalog.inventory.internal.adapter.outbound;

import com.grab.app.catalog.inventory.internal.adapter.outbound.mapper.InventoryDiscoveryQueryMapperImpl;
import com.grab.app.catalog.inventory.internal.config.InventoryReadTransactional;
import com.inventory.application.model.read.ListSkusAtLocationQuery;
import com.inventory.application.port.inbound.GetDiscoveryInventoryFactsUseCase;
import com.inventory.application.port.inbound.ListSkusAtLocationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryDiscoveryQueryAdapterTest {

    @Mock
    private GetDiscoveryInventoryFactsUseCase getDiscoveryInventoryFactsUseCase;
    @Mock
    private ListSkusAtLocationUseCase listSkusAtLocationUseCase;

    private InventoryDiscoveryQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryDiscoveryQueryAdapter(
                getDiscoveryInventoryFactsUseCase,
                listSkusAtLocationUseCase,
                new InventoryDiscoveryQueryMapperImpl()
        );
    }

    @Test
    void skusAtLocation_delegatesToInventoryUseCase() {
        when(listSkusAtLocationUseCase.execute(new ListSkusAtLocationQuery("location-1")))
                .thenReturn(List.of("SKU-1", "SKU-2"));

        List<String> skus = adapter.skusAtLocation("location-1");

        assertThat(skus).containsExactly("SKU-1", "SKU-2");
        verify(listSkusAtLocationUseCase).execute(new ListSkusAtLocationQuery("location-1"));
    }

    @Test
    void methodsAreAnnotatedWithInventoryReadTransactional() throws NoSuchMethodException {
        Method availability = InventoryDiscoveryQueryAdapter.class.getMethod(
                "discoveryAvailability",
                com.grab.framework.id.Id.class,
                String.class,
                com.grab.framework.id.Id.class
        );
        Method skusAtLocation = InventoryDiscoveryQueryAdapter.class.getMethod("skusAtLocation", String.class);

        assertThat(availability.isAnnotationPresent(InventoryReadTransactional.class)).isTrue();
        assertThat(skusAtLocation.isAnnotationPresent(InventoryReadTransactional.class)).isTrue();
    }
}
