package com.grab.service.sellerbff.internal.adapter.outbound.message;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.rpc.adapter.MutualTlsRmiClient;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegisterUserRequest;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegistrationStatus;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcFailure;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.SellerRegisterUserMessageMapper;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.rmi.RemoteException;

@Component
public class SellerAuthMessageAdapter implements SellerAuthOutboundPort {
    private static final Logger LOG = Loggers.getLogger(SellerAuthMessageAdapter.class);

    private final String accountHost;
    private final int registrationPort;
    private final SellerRegisterUserMessageMapper mapper;
    private final MutualTlsRmiClient transport = new MutualTlsRmiClient(1500, 4000);

    public SellerAuthMessageAdapter(
            SellerRegisterUserMessageMapper mapper,
            @Value("${grab.seller-oidc.account-host:account-service}") String accountHost,
            @Value("${grab.seller-oidc.registration-port:1200}") int registrationPort,
            @Value("${SELLER_AUTH_CLIENT_KEYSTORE}") Path keyStore,
            @Value("${SELLER_AUTH_CLIENT_KEY_PASSWORD}") String keyPassword,
            @Value("${SELLER_AUTH_CLIENT_TRUSTSTORE}") Path trustStore,
            @Value("${SELLER_AUTH_CLIENT_TRUST_PASSWORD}") String trustPassword) {
        MutualTlsRmiClient.configure(new SslMaterial(keyStore, keyPassword, trustStore, trustPassword));
        this.accountHost = accountHost;
        this.registrationPort = registrationPort;
        this.mapper = mapper;
    }

    @Override
    public SellerUserRegistrationResult registerUser(String email, String password) {
        RegisterUserRequest request = mapper.toMessage(email, password);
        try {
            SellerRegistrationPort registration = transport.lookup(accountHost, registrationPort, SellerRegistrationPort.class);
            var result = registration.registerUser(request);
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
