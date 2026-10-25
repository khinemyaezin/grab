package com.grab.app.channeldiscovery.storefrontquery.internal.query.handler;

import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.discovery.application.port.inbound.ResolveShoppingContextUseCase;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveShoppingContextQueryHandlerTest {
    @Mock
    private ResolveShoppingContextUseCase useCase;

    @InjectMocks
    private ResolveShoppingContextQueryHandler handler;

    @Test
    void handle_delegatesToResolverUseCase() {
        ResolveShoppingContextQuery query = new ResolveShoppingContextQuery("pk_token", "SG", "en-SG");
        ResolveShoppingContextResult expected = new ResolveShoppingContextResult(
                new ShoppingContext(new CommonId("channel-1"), new CommonId("region-1"), "SG", "USD", "en-SG"),
                true
        );
        when(useCase.execute(query)).thenReturn(expected);

        ResolveShoppingContextResult result = handler.handle(query);

        assertThat(result).isSameAs(expected);
        verify(useCase).execute(query);
        assertThat(handler.getQueryType()).isEqualTo(ResolveShoppingContextQuery.class);
    }
}
