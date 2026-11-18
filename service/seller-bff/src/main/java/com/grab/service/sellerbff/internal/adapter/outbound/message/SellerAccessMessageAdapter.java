package com.grab.service.sellerbff.internal.adapter.outbound.message;

import com.grab.service.contract.message.account.identity.SellerAccessPort;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcFailure;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcProperties;
import com.grab.service.sellerbff.port.outbound.SellerAccessOutboundPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
public final class SellerAccessMessageAdapter implements SellerAccessOutboundPort {
    private final SellerOidcProperties properties;
    private final SellerAccountTransport transport;
    private final MeterRegistry metrics;

    public SellerAccessMessageAdapter(SellerOidcProperties properties, SellerAccountTransport transport,
                                      MeterRegistry metrics) {
        this.properties = properties;
        this.transport = transport;
        this.metrics = metrics;
    }

    @Override
    public AccessState resolve(String issuer, String subject, String assignmentId, Intent intent) {
        Timer.Sample sample = Timer.start(metrics);
        try {
            SellerAccessPort port = transport.lookup(properties.accountHost(), properties.registrationPort(),
                    SellerAccessPort.class);
            SellerAccessPort.AccessRequest request = new SellerAccessPort.AccessRequest(issuer, subject,
                    assignmentId, SellerAccessPort.Intent.valueOf(intent.name()));
            SellerAccessPort.AccessState result = port.resolve(request);
            AccessState state = new AccessState(Status.valueOf(result.status().name()), result.userId(), result.email(),
                    result.authenticationVersion(), result.assignmentId(), result.scopeKey(), result.scopeId(),
                    result.roles(), result.authorities(), result.merchantRevision(), result.storefrontRevision());
            metrics.counter("seller.account.access", "outcome", state.status().name()).increment();
            return state;
        } catch (RemoteException exception) {
            metrics.counter("seller.account.access", "outcome", "unavailable").increment();
            throw SellerOidcFailure.unavailable();
        } finally {
            sample.stop(metrics.timer("seller.account.access.duration"));
        }
    }
}
