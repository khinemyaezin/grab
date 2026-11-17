package com.grab.service.sellerbff.internal.adapter.outbound.message;

import lombok.SneakyThrows;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.rpc.adapter.MutualTlsRmiClient;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import java.nio.file.Path;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcFailure;
import org.springframework.beans.factory.annotation.Value;

import java.rmi.RemoteException;
import com.grab.service.contract.message.account.identity.AuthenticationPort;
import com.grab.service.contract.message.account.identity.AuthenticationPort.AuthTokensResult;
import com.grab.service.contract.message.account.identity.AuthenticationPort.LoginMessage;
import com.grab.service.contract.message.account.identity.AuthenticationPort.LogoutMessage;
import com.grab.service.contract.message.account.identity.AuthenticationPort.RefreshTokenMessage;
import com.grab.service.contract.message.account.identity.AuthenticationPort.RegisterUserMessage;
import com.grab.service.contract.message.account.identity.AuthenticationPort.UserRegistrationResult;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.SellerLoginMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.SellerLogoutMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.SellerRefreshTokenMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.SellerRegisterUserMessageMapper;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import org.springframework.stereotype.Component;

@Component
public class SellerAuthMessageAdapter implements SellerAuthOutboundPort {
    private static final Logger LOG = Loggers.getLogger(SellerAuthMessageAdapter.class);

    private final AuthenticationPort authenticationPort;
    private final boolean oidc;
    private final String accountHost;
    private final int registrationPort;
    private final SellerLoginMessageMapper loginMessageMapper;
    private final SellerRefreshTokenMessageMapper refreshTokenMessageMapper;
    private final SellerLogoutMessageMapper logoutMessageMapper;
    private final SellerRegisterUserMessageMapper registerUserMessageMapper;

    public SellerAuthMessageAdapter(
            AuthenticationPort authenticationPort,
            SellerLoginMessageMapper loginMessageMapper,
            SellerRefreshTokenMessageMapper refreshTokenMessageMapper,
            SellerLogoutMessageMapper logoutMessageMapper,
            SellerRegisterUserMessageMapper registerUserMessageMapper,
            @Value("${grab.seller-oidc.enabled:false}") boolean oidc,
            @Value("${grab.seller-oidc.account-host:localhost}") String accountHost,
            @Value("${grab.seller-oidc.registration-port:1200}") int registrationPort,
            @Value("${SELLER_AUTH_CLIENT_KEYSTORE:}") String keyStore,
            @Value("${SELLER_AUTH_CLIENT_KEY_PASSWORD:}") String keyPassword,
            @Value("${SELLER_AUTH_CLIENT_TRUSTSTORE:}") String trustStore,
            @Value("${SELLER_AUTH_CLIENT_TRUST_PASSWORD:}") String trustPassword) {
        if (oidc) {
            Path keys = Path.of(keyStore);
            Path trust = Path.of(trustStore);
            MutualTlsRmiClient.configure(new SslMaterial(keys, keyPassword, trust, trustPassword));
        }
        this.oidc = oidc;
        this.accountHost = accountHost;
        this.registrationPort = registrationPort;
        this.authenticationPort = authenticationPort;
        this.loginMessageMapper = loginMessageMapper;
        this.refreshTokenMessageMapper = refreshTokenMessageMapper;
        this.logoutMessageMapper = logoutMessageMapper;
        this.registerUserMessageMapper = registerUserMessageMapper;
    }

    @Override
    @SneakyThrows
    public SellerAuthTokensResult login(String email, String password) {
        LoginMessage message = loginMessageMapper.toMessage(email, password);
        AuthTokensResult result = authenticationPort.login(message);
        return loginMessageMapper.toResult(result);
    }

    @Override
    @SneakyThrows
    public SellerAuthTokensResult refreshToken(String refreshToken) {
        RefreshTokenMessage message = refreshTokenMessageMapper.toMessage(refreshToken);
        AuthTokensResult result = authenticationPort.refreshToken(message);
        return refreshTokenMessageMapper.toResult(result);
    }

    @Override
    @SneakyThrows
    public void logout(String refreshToken) {
        LogoutMessage message = logoutMessageMapper.toMessage(refreshToken);
        authenticationPort.logout(message);
    }

    @Override
    @SneakyThrows
    public SellerUserRegistrationResult registerUser(String email, String password) {
        RegisterUserMessage message = registerUserMessageMapper.toMessage(email, password);
        UserRegistrationResult result = oidc ? registerProtected(message) : authenticationPort.registerUser(message);
        return registerUserMessageMapper.toResult(result);
    }
    private UserRegistrationResult registerProtected(RegisterUserMessage request) {
        MutualTlsRmiClient transport = new MutualTlsRmiClient(1500, 4000);
        try {
            SellerRegistrationPort registration = transport.lookup(accountHost, registrationPort, SellerRegistrationPort.class);
            return registration.registerUser(request);
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

