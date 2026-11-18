package com.grab.service.account.identity.internal.config;

import com.grab.rpc.adapter.MutualTlsRmiEndpoint;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort;
import com.grab.service.contract.message.account.identity.SellerAccessPort;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "grab.seller-authentication.rpc.enabled", havingValue = "true")
public class SellerAuthenticationTransportConfig {
    @Bean
    MutualTlsRmiEndpoint issuerAuthenticationEndpoint(
            SellerAuthenticationPort assignments, AccountAuthenticationPort account,
            @Value("${grab.rpc.bind-address:127.0.0.1}") String bind,
            @Value("${grab.rpc.advertised-host:localhost}") String host,
            @Value("${grab.seller-authentication.rpc.issuer-port:1199}") int port,
            @Value("${grab.seller-authentication.rpc.keystore}") Path keyStore,
            @Value("${grab.seller-authentication.rpc.key-password}") String keyPassword,
            @Value("${grab.seller-authentication.rpc.issuer-truststore}") Path trustStore,
            @Value("${grab.seller-authentication.rpc.trust-password}") String trustPassword) {
        SslMaterial material = new SslMaterial(keyStore, keyPassword, trustStore, trustPassword);
        return new MutualTlsRmiEndpoint(bind, host, port, material,
                Map.of(SellerAuthenticationPort.class, assignments, AccountAuthenticationPort.class, account));
    }

    @Bean
    MutualTlsRmiEndpoint sellerRegistrationEndpoint(
            SellerRegistrationPort provider, SellerAccessPort sellerAccess,
            @Value("${grab.rpc.bind-address:127.0.0.1}") String bind,
            @Value("${grab.rpc.advertised-host:localhost}") String host,
            @Value("${grab.seller-authentication.rpc.registration-port:1200}") int port,
            @Value("${grab.seller-authentication.rpc.keystore}") Path keyStore,
            @Value("${grab.seller-authentication.rpc.key-password}") String keyPassword,
            @Value("${grab.seller-authentication.rpc.bff-truststore}") Path trustStore,
            @Value("${grab.seller-authentication.rpc.trust-password}") String trustPassword) {
        SslMaterial material = new SslMaterial(keyStore, keyPassword, trustStore, trustPassword);
        return new MutualTlsRmiEndpoint(bind, host, port, material,
                Map.of(SellerRegistrationPort.class, provider, SellerAccessPort.class, sellerAccess));
    }
}
