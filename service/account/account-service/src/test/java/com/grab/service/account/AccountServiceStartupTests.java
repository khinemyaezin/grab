package com.grab.service.account;

import com.grab.rpc.adapter.RmiProviderLifecycle;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import javax.management.ObjectName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceStartupTests {

    @Test
    void migratesAllAccountSchemasAndRestartsAgainstTheSameDatabase() {
        try (PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")) {
            database.start();
            assertAccountRuntimeStarts(database);
            assertAccountRuntimeStarts(database);
        }
    }

    @Test
    void productionProfileValidatesSchemasAndKeepsJwtKeyStableAcrossRestarts() throws Exception {
        try (PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")) {
            database.start();
            Path keyStore = createKeyStore();
            try {
                PublicKey firstKey = assertProductionRuntimeStarts(database, keyStore);
                PublicKey restartedKey = assertProductionRuntimeStarts(database, keyStore);
                assertThat(restartedKey.getEncoded()).containsExactly(firstKey.getEncoded());
            } finally {
                Files.deleteIfExists(keyStore);
            }
        }
    }

    @Test
    void productionProfileRequiresDatabaseCredentialsAndAValidJwtKeyStore() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(AccountServiceApplication.class)
                .run("--spring.profiles.active=prod", "--grab.rpc.enabled=false"))
                .isNotNull();

        try (PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")) {
            database.start();
            assertThatThrownBy(() -> new SpringApplicationBuilder(AccountServiceApplication.class)
                    .run(productionArguments(database, "/missing/account-service.p12", "invalid")))
                    .isNotNull();
        }
    }

    private void assertAccountRuntimeStarts(PostgreSQLContainer<?> database) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(AccountServiceApplication.class)
                .run(
                        "--account.datasource.url=" + database.getJdbcUrl(),
                        "--account.datasource.username=" + database.getUsername(),
                        "--account.datasource.password=" + database.getPassword(),
                        "--grab.rpc.enabled=false")) {
            assertThat(context.getBeansOfType(RmiProviderLifecycle.class)).isEmpty();
            for (String module : new String[]{"identity", "merchant", "customer"}) {
                Flyway flyway = context.getBean(module + "Flyway", Flyway.class);
                assertThat(flyway.getConfiguration().getDefaultSchema()).isEqualTo(module);
                assertThat(flyway.info().applied()).anyMatch(migration -> migration.getVersion() != null);
                assertThat(flyway.info().pending()).isEmpty();

                EntityManagerFactory entityManagerFactory =
                        context.getBean(module + "EntityManagerFactory", EntityManagerFactory.class);
                assertThat(entityManagerFactory.isOpen()).isTrue();

                ModuleJobScheduler scheduler = context.getBean(module + "JobScheduler", ModuleJobScheduler.class);
                assertThat(scheduler.isRunning()).isTrue();
            }
        }
    }

    private PublicKey assertProductionRuntimeStarts(PostgreSQLContainer<?> database, Path keyStore) throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(AccountServiceApplication.class)
                .run(productionArguments(database, keyStore.toString(), "account-test-password"))) {
            assertThat(context.getEnvironment().getActiveProfiles()).contains("prod");
            assertThat(context).isNotInstanceOf(WebServerApplicationContext.class);
            var mbeanServer = ManagementFactory.getPlatformMBeanServer();
            assertThat(mbeanServer.isRegistered(new ObjectName("org.springframework.boot:type=Endpoint,name=Health")))
                    .isTrue();
            assertThat(mbeanServer.isRegistered(new ObjectName("org.springframework.boot:type=Endpoint,name=Metrics")))
                    .isTrue();
            for (String module : new String[]{"identity", "merchant", "customer"}) {
                Flyway flyway = context.getBean(module + "Flyway", Flyway.class);
                assertThat(flyway.info().pending()).isEmpty();
                EntityManagerFactory entityManagerFactory =
                        context.getBean(module + "EntityManagerFactory", EntityManagerFactory.class);
                assertThat(entityManagerFactory.isOpen()).isTrue();
                assertThat(context.getEnvironment().getProperty(module + ".jpa.hibernate.hbm2ddl.auto"))
                        .isEqualTo("validate");
            }
            return context.getBean(PublicKey.class);
        }
    }

    private String[] productionArguments(PostgreSQLContainer<?> database, String keyStorePath, String keyStorePassword) {
        return new String[]{
                "--spring.profiles.active=prod",
                "--ACCOUNT_DB_URL=" + database.getJdbcUrl(),
                "--ACCOUNT_DB_USERNAME=" + database.getUsername(),
                "--ACCOUNT_DB_PASSWORD=" + database.getPassword(),
                "--JWT_KEYSTORE_PATH=" + keyStorePath,
                "--JWT_KEYSTORE_PASSWORD=" + keyStorePassword,
                "--grab.rpc.enabled=false"
        };
    }

    private Path createKeyStore() throws IOException, InterruptedException {
        Path keyStore = Files.createTempFile("account-service", ".p12");
        Files.delete(keyStore);
        Path keytool = Path.of(System.getProperty("java.home"), "bin", "keytool");
        Process process = new ProcessBuilder(
                keytool.toString(), "-genkeypair", "-alias", "account-service", "-keyalg", "RSA",
                "-keysize", "2048", "-storetype", "PKCS12", "-keystore", keyStore.toString(),
                "-storepass", "account-test-password", "-keypass", "account-test-password",
                "-dname", "CN=account-service", "-validity", "3650", "-noprompt"
        ).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Could not create PKCS#12 test key store: " + output);
        }
        return keyStore;
    }
}
