package com.grab.service.sellerbff.internal.adapter.outbound.message;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegisterUserRequest;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegistrationStatus;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegistrationResult;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcFailure;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.SellerRegisterUserMessageMapper;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
public class SellerAuthMessageAdapter implements SellerAuthOutboundPort {
    private static final Logger LOG = Loggers.getLogger(SellerAuthMessageAdapter.class);

    private final com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcProperties properties;
    private final SellerRegisterUserMessageMapper mapper;
    private final SellerAccountTransport transport;

    public SellerAuthMessageAdapter(
            SellerRegisterUserMessageMapper mapper,
            com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcProperties properties,
            SellerAccountTransport transport) {
        this.properties = properties;
        this.mapper = mapper;
        this.transport = transport;
    }

    @Override
    public SellerUserRegistrationResult registerUser(String email, String password) {
        RegisterUserRequest request = mapper.toMessage(email, password);
        try {
            SellerRegistrationPort registration = transport.lookup(properties.accountHost(), properties.registrationPort(), SellerRegistrationPort.class);
            RegistrationResult result = registration.registerUser(request);
            if (result.status() == RegistrationStatus.EMAIL_EXISTS) {
                throw SellerOidcFailure.emailExists();
            }
            if (result.status() == RegistrationStatus.INVALID_INPUT) {
                throw SellerOidcFailure.invalidRegistration();
            }
            return mapper.toResult(result);
        } catch (RemoteException exception) {
            Throwable failure = exception;
            while (failure.getCause() != null && failure.getCause() != failure) {
                failure = failure.getCause();
            }
            LOG.warn("Protected Account registration RPC failed cause={}", failure.getClass().getSimpleName());
            throw SellerOidcFailure.unavailable();
        }
    }
}
