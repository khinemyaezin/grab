package com.grab.app.productmerchandising.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.inventory.application.model.write.ProjectProductVariantViewCommand;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.UpsertVariant;
import com.inventory.application.model.write.ProjectProductVariantViewResult;
import com.inventory.application.model.write.ProjectProductVariantViewResult.VariantProjected;
import com.inventory.application.port.inbound.ProjectProductVariantViewUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectProductVariantViewCommandHandlerTest {
    private static final Id PRODUCT_ID = new CommonId("product-1");
    private static final Id VARIANT_ID = new CommonId("variant-1");
    @Mock
    private ProjectProductVariantViewUseCase useCase;
    @InjectMocks
    private ProjectProductVariantViewCommandHandler handler;

    @Test
    void handle_validCommand_delegatesToUseCase() {
        ProjectProductVariantViewCommand command = command();
        VariantProjected notice = new VariantProjected(PRODUCT_ID, VARIANT_ID, "SKU1");
        ProjectProductVariantViewResult result = new ProjectProductVariantViewResult(List.of(notice));
        when(useCase.execute(command)).thenReturn(result);

        ProjectProductVariantViewResult actual = handler.handle(command);

        assertThat(actual).isSameAs(result);
        verify(useCase).execute(command);
    }

    @Test
    void handle_failedProjection_propagatesException() {
        ProjectProductVariantViewCommand command = command();
        IllegalStateException failure = new IllegalStateException("persistence failed");
        when(useCase.execute(command)).thenThrow(failure);

        assertThatThrownBy(() -> handler.handle(command)).isSameAs(failure);
    }

    @Test
    void getCommandType_returnsProjectProductVariantViewCommandClass() {
        assertThat(handler.getCommandType()).isEqualTo(ProjectProductVariantViewCommand.class);
    }

    private ProjectProductVariantViewCommand command() {
        UpsertVariant update = new UpsertVariant(PRODUCT_ID, VARIANT_ID, "SKU1", "Product", true, true);
        return new ProjectProductVariantViewCommand(update);
    }
}
