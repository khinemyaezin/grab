package com.grab.service.channel;

import com.grab.discovery.application.port.outbound.SalesChannelQueryPort;
import com.grab.framework.id.impl.CommonId;
import com.grab.rpc.adapter.RmiProviderLifecycle;
import com.grab.service.channel.internal.bootstrap.BootstrapRunner;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.service.contract.message.channel.region.RegionPort;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StandaloneRuntimeTest {
    @Test
    void migratesAndRestartsInProduction() throws Exception {
        try (var database = new PostgreSQLContainer<>("postgres:16-alpine")) {
            database.start();
            try (var startup = start(database, 0)) {
                checkRuntime(startup);
                assertThat(startup.getBeansOfType(RmiProviderLifecycle.class)).isEmpty();
            }

            int port;
            try (var socket = new ServerSocket(0)) {
                port = socket.getLocalPort();
            }
            try (var restarted = start(database, port)) {
                checkRuntime(restarted);
                invokeRemoteRead(port);
                checkLocalChannelLookupInDiscoveryTransaction(restarted);
                database.stop();
                var health = restarted.getBean(HealthEndpoint.class);
                assertThat(health.healthForPath("readiness").getStatus()).isEqualTo(Status.DOWN);
                assertThat(health.healthForPath("liveness").getStatus()).isEqualTo(Status.UP);
            }
        }
    }

    @Test
    void productionRequiresDatabaseSettings() {
        var environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        assertThatThrownBy(() -> new SpringApplicationBuilder(ChannelServiceApplication.class)
                .environment(environment).run("--spring.profiles.active=prod", "--grab.rpc.enabled=false"))
                .hasStackTraceContaining("CHANNEL_DB_URL");
    }

    private ConfigurableApplicationContext start(PostgreSQLContainer<?> database, int port) {
        return new SpringApplicationBuilder(ChannelServiceApplication.class).run(
                "--spring.profiles.active=prod",
                "--CHANNEL_DB_URL=" + database.getJdbcUrl(),
                "--CHANNEL_DB_USERNAME=" + database.getUsername(),
                "--CHANNEL_DB_PASSWORD=" + database.getPassword(),
                "--CHANNEL_HIKARI_CONNECTION_TIMEOUT=500",
                "--bootstrap.gate-readiness=true",
                "--grab.rpc.enabled=" + (port > 0),
                "--grab.rpc.registry-port=" + port);
    }

    private void checkRuntime(ConfigurableApplicationContext context) throws Exception {
        assertThat(context).isNotInstanceOf(WebServerApplicationContext.class);
        assertThat(context.getBeansOfType(javax.sql.DataSource.class)).hasSize(1);
        var expected = Map.of("salesChannel", 4, "region", 2, "discovery", 2);
        assertThat(context.getBeansOfType(Flyway.class)).hasSize(expected.size());
        for (var schema : expected.entrySet()) {
            String module = schema.getKey();
            var flyway = context.getBean(module + "Flyway", Flyway.class);
            flyway.validate();
            assertThat(flyway.info().applied()).filteredOn(migration -> migration.getVersion() != null)
                    .hasSize(schema.getValue());
            var factory = context.getBean(module + "EntityManagerFactory", EntityManagerFactory.class);
            assertThat(factory.getProperties()).containsEntry("hibernate.hbm2ddl.auto", "validate");
        }
        for (String module : List.of("salesChannel", "region", "discovery")) {
            var scheduler = context.getBean(module + "JobScheduler", ModuleJobScheduler.class);
            assertThat(scheduler.isRunning()).isTrue();
        }
        var jmx = ManagementFactory.getPlatformMBeanServer();
        assertThat(jmx.isRegistered(new ObjectName("org.springframework.boot:type=Endpoint,name=Health"))).isTrue();
        assertThat(jmx.isRegistered(new ObjectName("org.springframework.boot:type=Endpoint,name=Metrics"))).isTrue();
        var bootstrap = context.getBean(BootstrapRunner.class);
        await().until(bootstrap::isFinished);
        assertThat(bootstrap.isReadinessBlocked()).isFalse();
        var health = context.getBean(HealthEndpoint.class);
        assertThat(health.healthForPath("readiness").getStatus()).isEqualTo(Status.UP);
        assertThat(health.healthForPath("liveness").getStatus()).isEqualTo(Status.UP);
    }

    private void checkLocalChannelLookupInDiscoveryTransaction(ConfigurableApplicationContext context) {
        var channels = context.getBean(SalesChannelQueryPort.class);
        var manager = context.getBean("discoveryTransactionManager", PlatformTransactionManager.class);
        var transaction = new TransactionTemplate(manager);
        var missing = new CommonId("missing");
        Boolean enabled = transaction.execute(status -> channels.isEnabled(missing));
        assertThat(enabled).isFalse();
    }

    private void invokeRemoteRead(int port) throws Exception {
        var registry = LocateRegistry.getRegistry("127.0.0.1", port);
        var peer = (RegionPort) registry.lookup(RegionPort.class.getName());
        assertThat(peer.listSupportedCountryCodes()).isNotNull();
    }
}
