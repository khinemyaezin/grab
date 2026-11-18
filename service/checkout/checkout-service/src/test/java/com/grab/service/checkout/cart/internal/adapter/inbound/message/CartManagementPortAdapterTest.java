package com.grab.service.checkout.cart.internal.adapter.inbound.message;

import com.grab.service.checkout.TestSupport;
import com.grab.service.checkout.cart.internal.adapter.inbound.message.mapper.AddItemToCartPortMapper;
import com.grab.service.checkout.cart.internal.adapter.inbound.message.mapper.GetCurrentCartPortMapper;
import com.grab.service.contract.message.checkout.cart.CartManagementPort.AddItemMessage;
import com.grab.service.contract.message.checkout.cart.CartManagementPort.GetCurrentCartMessage;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.cart.application.model.write.AddItemToCartCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CartManagementPortAdapterTest {

    @Test
    void dispatchesCartReadAndWriteOperationsOnCheckoutBusesAndPreservesNullResults() throws Exception {
        TestSupport.RecordingCommandBus commandBus = new TestSupport.RecordingCommandBus();
        TestSupport.RecordingQueryBus queryBus = new TestSupport.RecordingQueryBus();
        CartManagementPortAdapter adapter = new CartManagementPortAdapter(
                commandBus,
                queryBus,
                TestSupport.mapper(GetCurrentCartPortMapper.class),
                TestSupport.mapper(AddItemToCartPortMapper.class));

        var readMessage = new GetCurrentCartMessage("guest", "channel-1", "region-1");
        assertThat(adapter.getCurrentCart(readMessage)).isNull();
        assertThat(queryBus.dispatchedQuery()).isInstanceOf(GetCurrentCartQuery.class);

        var writeMessage = new AddItemMessage("guest", "channel-1", "region-1", "USD", "variant-1", 1,
                BigDecimal.ONE);
        adapter.addItem(writeMessage);
        assertThat(commandBus.dispatchedCommand()).isInstanceOf(AddItemToCartCommand.class);
    }
}
