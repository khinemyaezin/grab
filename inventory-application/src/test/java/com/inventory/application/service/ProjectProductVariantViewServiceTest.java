package com.inventory.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.inventory.application.model.write.ProductVariantProjection;
import com.inventory.application.model.write.ProjectProductVariantViewCommand;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.DeleteProduct;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.RenameProduct;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.SetVariantStatus;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.UpsertVariant;
import com.inventory.application.model.write.ProjectProductVariantViewResult;
import com.inventory.application.policy.ProductVariantProjectionPolicy;
import com.inventory.application.port.outbound.ProductVariantProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectProductVariantViewServiceTest {
    private static final Id PRODUCT_ID = new CommonId("product-1");
    private static final Id VARIANT_ID = new CommonId("variant-1");
    @Mock
    private ProductVariantProjectionRepository projections;
    @Captor
    private ArgumentCaptor<ProductVariantProjection> projectionCaptor;
    private ProjectProductVariantViewService service;

    @BeforeEach
    void setUp() {
        ProductVariantProjectionPolicy policy = new ProductVariantProjectionPolicy();
        service = new ProjectProductVariantViewService(projections, policy);
    }

    @Test
    void execute_addedVariant_upsertsFactsAndReturnsNotification() {
        UpsertVariant update = new UpsertVariant(PRODUCT_ID, VARIANT_ID, "SKU1", "Product", true, true);
        ProjectProductVariantViewCommand command = new ProjectProductVariantViewCommand(update);

        ProjectProductVariantViewResult result = service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        ProductVariantProjection saved = projectionCaptor.getValue();
        assertThat(saved.variantId()).isEqualTo(VARIANT_ID);
        assertThat(saved.productId()).isEqualTo(PRODUCT_ID);
        assertThat(saved.productName()).isEqualTo("Product");
        assertThat(saved.manageInventory()).isTrue();
        assertThat(saved.status()).isEqualTo("ACTIVE");
        assertThat(result.notifications()).hasSize(1);
        assertThat(result.notifications().getFirst().sku()).isEqualTo("SKU1");
    }

    @Test
    void execute_partialVariantUpdate_preservesPresentationAndDeletedStatus() {
        ProductVariantProjection previous = projection("DELETED");
        when(projections.findByVariant(VARIANT_ID)).thenReturn(Optional.of(previous));
        UpsertVariant update = new UpsertVariant(PRODUCT_ID, VARIANT_ID, "NEW-SKU", null, false, false);
        ProjectProductVariantViewCommand command = new ProjectProductVariantViewCommand(update);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        ProductVariantProjection saved = projectionCaptor.getValue();
        assertThat(saved.productName()).isEqualTo("Existing name");
        assertThat(saved.status()).isEqualTo("DELETED");
        assertThat(saved.sku()).isEqualTo("NEW-SKU");
        assertThat(saved.manageInventory()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void execute_variantLifecycleChange_updatesOnlyStatus(boolean active) {
        ProductVariantProjection previous = projection("ACTIVE");
        when(projections.findByVariant(VARIANT_ID)).thenReturn(Optional.of(previous));
        SetVariantStatus update = new SetVariantStatus(VARIANT_ID, active);
        ProjectProductVariantViewCommand command = new ProjectProductVariantViewCommand(update);

        ProjectProductVariantViewResult result = service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        ProductVariantProjection saved = projectionCaptor.getValue();
        String expectedStatus = active ? "ACTIVE" : "DELETED";
        assertThat(saved.status()).isEqualTo(expectedStatus);
        assertThat(saved.productName()).isEqualTo(previous.productName());
        assertThat(saved.sku()).isEqualTo(previous.sku());
        assertThat(result.notifications()).isEmpty();
    }

    @Test
    void execute_missingVariantLifecycleChange_leavesProjectionAbsent() {
        SetVariantStatus update = new SetVariantStatus(VARIANT_ID, false);
        ProjectProductVariantViewCommand command = new ProjectProductVariantViewCommand(update);

        service.execute(command);

        verify(projections, never()).save(any());
    }

    @Test
    void execute_renamedProduct_preservesOtherFacts() {
        ProductVariantProjection previous = projection("ACTIVE");
        when(projections.findByProduct(PRODUCT_ID)).thenReturn(List.of(previous));
        RenameProduct update = new RenameProduct(PRODUCT_ID, "Renamed");
        ProjectProductVariantViewCommand command = new ProjectProductVariantViewCommand(update);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        ProductVariantProjection saved = projectionCaptor.getValue();
        assertThat(saved.productName()).isEqualTo("Renamed");
        assertThat(saved.sku()).isEqualTo(previous.sku());
        assertThat(saved.status()).isEqualTo("ACTIVE");
    }

    @Test
    void execute_missingProductName_keepsExistingProjection() {
        RenameProduct update = new RenameProduct(PRODUCT_ID, null);
        ProjectProductVariantViewCommand command = new ProjectProductVariantViewCommand(update);

        service.execute(command);

        verifyNoInteractions(projections);
    }

    @Test
    void execute_deletedProduct_marksExistingVariantDeleted() {
        ProductVariantProjection previous = projection("ACTIVE");
        when(projections.findByProduct(PRODUCT_ID)).thenReturn(List.of(previous));
        DeleteProduct update = new DeleteProduct(PRODUCT_ID);
        ProjectProductVariantViewCommand command = new ProjectProductVariantViewCommand(update);

        service.execute(command);

        verify(projections).save(projectionCaptor.capture());
        assertThat(projectionCaptor.getValue().status()).isEqualTo("DELETED");
    }

    private ProductVariantProjection projection(String status) {
        return new ProductVariantProjection(PRODUCT_ID, VARIANT_ID, "SKU1", "Existing name", true, status);
    }
}
