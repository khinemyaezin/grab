package com.grab.service.checkout;

import com.grab.rpc.adapter.RmiProviderLifecycle;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.service.contract.message.checkout.cart.CartManagementPort;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

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
                invokeRemoteRead(restarted, port);
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
        assertThatThrownBy(() -> new SpringApplicationBuilder(CheckoutServiceApplication.class)
                .environment(environment).run("--spring.profiles.active=prod", "--grab.rpc.enabled=false"))
                .hasStackTraceContaining("CHECKOUT_DB_URL");
    }

    private ConfigurableApplicationContext start(PostgreSQLContainer<?> database, int port) {
        return new SpringApplicationBuilder(CheckoutServiceApplication.class).run(
                "--spring.profiles.active=prod",
                "--CHECKOUT_DB_URL=" + database.getJdbcUrl(),
                "--CHECKOUT_DB_USERNAME=" + database.getUsername(),
                "--CHECKOUT_DB_PASSWORD=" + database.getPassword(),
                "--CHECKOUT_HIKARI_CONNECTION_TIMEOUT=500",
                "--grab.rpc.enabled=" + (port > 0),
                "--grab.rpc.registry-port=" + port);
    }

    private void checkRuntime(ConfigurableApplicationContext context) throws Exception {
        assertThat(context).isNotInstanceOf(WebServerApplicationContext.class);
        assertThat(context.getBeansOfType(javax.sql.DataSource.class)).hasSize(1);
        var expected = Map.of("cart", 2);
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
        for (String module : List.of("cart")) {
            var scheduler = context.getBean(module + "JobScheduler", ModuleJobScheduler.class);
            assertThat(scheduler.isRunning()).isTrue();
        }
        var jmx = ManagementFactory.getPlatformMBeanServer();
        assertThat(jmx.isRegistered(new ObjectName("org.springframework.boot:type=Endpoint,name=Health"))).isTrue();
        assertThat(jmx.isRegistered(new ObjectName("org.springframework.boot:type=Endpoint,name=Metrics"))).isTrue();
        var health = context.getBean(HealthEndpoint.class);
        assertThat(health.healthForPath("readiness").getStatus()).isEqualTo(Status.UP);
        assertThat(health.healthForPath("liveness").getStatus()).isEqualTo(Status.UP);
    }

    private void invokeRemoteRead(ConfigurableApplicationContext context, int port) throws Exception {
        var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
        jdbc.update("""
                INSERT INTO cart.carts (created_at, updated_at, version, channel_type, currency_code,
                                        guest_token, region_id, sales_channel_id, status, uuid)
                VALUES (now(), now(), 0, 'MARKETPLACE', 'USD', 'test-guest', 'test-region', 'test-channel',
                        'OPEN', 'test-cart')
                """);
        var registry = LocateRegistry.getRegistry("127.0.0.1", port);
        assertThat(registry.list()).containsExactly(CartManagementPort.class.getName());
        var peer = (CartManagementPort) registry.lookup(CartManagementPort.class.getName());
        var request = new CartManagementPort.GetCurrentCartMessage("test-guest", "test-channel", "test-region");
        assertThat(peer.getCurrentCart(request).cartId()).isEqualTo("test-cart");
        var missing = new CartManagementPort.GetCurrentCartMessage("missing", "test-channel", "test-region");
        assertThatThrownBy(() -> peer.getCurrentCart(missing))
                .isInstanceOf(RemoteException.class).hasMessageContaining("cart");
        var add = new CartManagementPort.AddItemMessage("test-guest", "test-channel", "test-region",
                "USD", "variant", 1, null);
        assertThatThrownBy(() -> peer.addItem(add))
                .isInstanceOf(RemoteException.class).hasMessageContaining("cart.domain.channel.disabled");
    }
}
