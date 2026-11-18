package com.grab.service.account;

import com.grab.framework.id.impl.CommonId;
import com.grab.rpc.adapter.MutualTlsRmiClient;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import com.grab.service.account.merchant.port.SellerScopeRevisionQuery;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort;
import com.grab.service.contract.message.account.identity.AuthenticationPort.RegisterUserMessage;
import com.grab.service.contract.message.account.identity.IdentityAdminPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.VerifiedUser;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.Credentials;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.StateRequest;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegisterUserRequest;
import com.grab.service.contract.message.account.identity.SellerRegistrationPort.RegistrationStatus;
import com.grab.service.contract.message.account.merchant.MerchantAccountPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class SellerAuthenticationIntegrationTest {
    private static final String CERTIFICATE_PASSWORD = "local-integration-only";
    private static final String SELLER = "test-seller@example.com";
    private static final String PASSWORD = "seller-test-only-password";

    @TempDir Path files;

    @Test
    void credentialsAndRegistrationUseSeparateAuthenticatedRmiBindings() throws Exception {
        Path server = key("server", "serverAuth");
        Path issuer = key("issuer", "clientAuth");
        Path bff = key("bff", "clientAuth");
        Path serverTrust = trust("server-trust", server);
        Path issuerTrust = trust("issuer-trust", issuer);
        Path bffTrust = trust("bff-trust", bff);
        int issuerPort = newPort();
        int bffPort = newPort();

        try (PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")) {
            database.start();
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(AccountServiceApplication.class)
                    .run("--account.datasource.url=" + database.getJdbcUrl(),
                            "--account.datasource.username=" + database.getUsername(),
                            "--account.datasource.password=" + database.getPassword(),
                            "--grab.rpc.enabled=false",
                            "--merchant.outbox.retry-delay-ms=100",
                            "--identity.outbox.retry-delay-ms=100",
                            "--grab.seller-authentication.rpc.enabled=true",
                            "--grab.seller-authentication.rpc.issuer-port=" + issuerPort,
                            "--grab.seller-authentication.rpc.registration-port=" + bffPort,
                            "--grab.seller-authentication.rpc.keystore=" + server,
                            "--grab.seller-authentication.rpc.key-password=" + CERTIFICATE_PASSWORD,
                            "--grab.seller-authentication.rpc.issuer-truststore=" + issuerTrust,
                            "--grab.seller-authentication.rpc.bff-truststore=" + bffTrust,
                            "--grab.seller-authentication.rpc.trust-password=" + CERTIFICATE_PASSWORD)) {
                MutualTlsRmiClient client = new MutualTlsRmiClient(2000, 4000);
                connectWith(bff, serverTrust);
                SellerRegistrationPort registration = client.lookup("localhost", bffPort, SellerRegistrationPort.class);
                var registered = registration.registerUser(new RegisterUserRequest(SELLER, PASSWORD));
                assertThat(registered.status()).isEqualTo(RegistrationStatus.CREATED);
                String userId = registered.id();
                assertThat(registration.registerUser(new RegisterUserRequest(SELLER, PASSWORD)).status())
                        .isEqualTo(RegistrationStatus.EMAIL_EXISTS);
                assertThat(registration.registerUser(new RegisterUserRequest("other@example.com", "x".repeat(73))).status())
                        .isEqualTo(RegistrationStatus.INVALID_INPUT);
                assertThatThrownBy(() -> client.lookup("localhost", issuerPort, SellerAuthenticationPort.class))
                        .isInstanceOf(RemoteException.class);

                connectWith(issuer, serverTrust);
                MutualTlsRmiClient freshClient = new MutualTlsRmiClient(2100, 4100);
                assertThatThrownBy(() -> freshClient.lookup("localhost", bffPort, SellerRegistrationPort.class))
                        .isInstanceOf(RemoteException.class);
                SellerAuthenticationPort auth = client.lookup("localhost", issuerPort, SellerAuthenticationPort.class);
                AccountAuthenticationPort shared = client.lookup("localhost", issuerPort, AccountAuthenticationPort.class);
                AccountAuthenticationPort.Credentials credentials = new AccountAuthenticationPort.Credentials(SELLER, PASSWORD);
                AccountAuthenticationPort.SubjectRequest subject = new AccountAuthenticationPort.SubjectRequest(userId);
                assertThat(shared.verify(credentials)).isEqualTo(shared.resolve(subject));
                assertThat(shared.resolve(subject).userId()).isEqualTo(userId);
                assertThat(auth.verify(new Credentials(SELLER, PASSWORD)).userId()).isEqualTo(userId);
                assertThat(auth.verify(new Credentials(SELLER, "wrong"))).isNull();
                assertThat(auth.resolve(new StateRequest(userId, null, true)).assignmentId()).isNull();

                IdentityAdminPort admin = context.getBean(IdentityAdminPort.class);
                CommonId seller = new CommonId(userId);
                admin.changeUserStatus(seller, "SUSPENDED");
                assertThat(auth.verify(new Credentials(SELLER, PASSWORD))).isNull();
                assertThat(auth.resolve(new StateRequest(userId, null, false))).isNull();
                assertThat(shared.verify(credentials)).isNull();
                assertThat(shared.resolve(subject)).isNull();
                admin.changeUserStatus(seller, "ACTIVE");
                assertThat(shared.resolve(subject).authenticationVersion()).isEqualTo(1);
                VerifiedUser reactivated = auth.verify(new Credentials(SELLER, PASSWORD));
                assertThat(reactivated.authenticationVersion()).isEqualTo(1);

                DataSource databaseSource = context.getBean("accountDataSource", DataSource.class);
                JdbcTemplate jdbc = new JdbcTemplate(databaseSource);
                assertMerchantRevocation(context, jdbc, auth, userId);
            }
        }
    }

    private void assertMerchantRevocation(ConfigurableApplicationContext context, JdbcTemplate jdbc,
                                          SellerAuthenticationPort auth, String userId) throws RemoteException {
        jdbc.update("""
                INSERT INTO merchant.merchant_accounts
                  (created_at, updated_at, version, applicant_user_id, display_name, merchant_type,
                   primary_market_country_code, status, uuid)
                VALUES (now(), now(), 0, ?, 'Merchant', 'C2C_SELLER', 'US', 'ACTIVE', 'merchant-scope')
                """, userId);
        jdbc.update("""
                INSERT INTO merchant.storefronts
                  (created_at, updated_at, version, merchant_id, name, slug, status, uuid)
                VALUES (now(), now(), 0, 'merchant-scope', 'Shop', 'shop-slug', 'ACTIVE', 'storefront-scope')
                """);
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            Integer count = jdbc.queryForObject("""
                    SELECT count(*) FROM identity.security_scope_definitions
                    WHERE scope_key = 'merchant.account' AND active
                    """, Integer.class);
            return Integer.valueOf(1).equals(count);
        });
        jdbc.update("""
                INSERT INTO identity.roles (active, assignable, code, name, role_kind, uuid)
                VALUES (true, true, 'SELLER_SCOPE_TEST', 'Seller', 'CUSTOM', 'seller-role')
                """);
        jdbc.update("""
                INSERT INTO identity.access_assignments
                  (created_at, updated_at, role_id, user_id, scope_id, scope_key, status, uuid)
                SELECT now(), now(), r.id, u.id, 'merchant-scope', 'merchant.account', 'ACTIVE', 'merchant-assignment'
                FROM identity.roles r CROSS JOIN identity.users u
                WHERE r.code = 'SELLER_SCOPE_TEST' AND u.uuid = ?
                """, userId);
        StateRequest login = new StateRequest(userId, null, true);
        assertThat(auth.resolve(login).scopeId()).isEqualTo("merchant-scope");
        StateRequest selected = new StateRequest(userId, "merchant-assignment", false);
        assertThat(auth.resolve(selected).merchantRevision()).isZero();
        SellerScopeRevisionQuery revisions = context.getBean(SellerScopeRevisionQuery.class);
        CommonId storefront = new CommonId("storefront-scope");
        assertThat(revisions.find("merchant.storefront", storefront)).isEqualTo(new SellerScopeRevisionQuery.Revision(0, 0));

        MerchantAccountPort merchants = context.getBean(MerchantAccountPort.class);
        CommonId merchant = new CommonId("merchant-scope");
        CommonId actor = new CommonId(userId);
        merchants.suspendMerchant(merchant, actor, "review");
        assertThat(auth.resolve(selected)).isNull();
        AccessAssignmentPort assignments = context.getBean(AccessAssignmentPort.class);
        assertThat(assignments.listAvailableContexts(actor)).isEmpty();
        assertThat(revisions.find("merchant.storefront", storefront)).isNull();
        merchants.reactivateMerchant(merchant, actor);
        assertThat(auth.resolve(selected).merchantRevision()).isEqualTo(1);
        assertThat(revisions.find("merchant.storefront", storefront)).isEqualTo(new SellerScopeRevisionQuery.Revision(1, 0));
        Long events = jdbc.queryForObject("""
                SELECT count(*) FROM merchant.outbox_record WHERE aggregate_type='MerchantAccount'
                  AND aggregate_id='merchant-scope' AND event_type LIKE '%MerchantSuspendedIntegrationEvent'
                """, Long.class);
        assertThat(events).isEqualTo(1L);
    }

    private void connectWith(Path key, Path trustedServer) {
        SslMaterial material = new SslMaterial(key, CERTIFICATE_PASSWORD, trustedServer, CERTIFICATE_PASSWORD);
        MutualTlsRmiClient.configure(material);
    }

    private Path key(String name, String usage) throws Exception {
        Path path = files.resolve(name + ".p12");
        keytool("-genkeypair", "-alias", name, "-keyalg", "RSA", "-keysize", "2048",
                "-dname", "CN=localhost", "-ext", "SAN=dns:localhost", "-ext", "EKU=" + usage,
                "-keystore", path.toString(), "-validity", "7", "-noprompt");
        return path;
    }

    private Path trust(String name, Path peer) throws Exception {
        Path certificate = files.resolve(name + ".cer");
        keytool("-exportcert", "-keystore", peer.toString(), "-file", certificate.toString(),
                "-alias", peer.getFileName().toString().replace(".p12", ""));
        Path store = files.resolve(name + ".p12");
        keytool("-importcert", "-alias", name, "-file", certificate.toString(),
                "-keystore", store.toString(), "-noprompt");
        return store;
    }

    private void keytool(String... arguments) throws Exception {
        Path tool = Path.of(System.getProperty("java.home"), "bin", "keytool");
        List<String> command = new ArrayList<>();
        command.add(tool.toString());
        command.addAll(List.of(arguments));
        command.addAll(List.of("-storetype", "PKCS12", "-storepass", CERTIFICATE_PASSWORD));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes());
        assertThat(process.waitFor()).withFailMessage(output).isZero();
    }

    private int newPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
