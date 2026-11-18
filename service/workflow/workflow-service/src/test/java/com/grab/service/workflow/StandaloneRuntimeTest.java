package com.grab.service.workflow;

import com.grab.framework.id.impl.CommonId;
import com.grab.rpc.adapter.RmiProviderLifecycle;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort;
import com.grab.service.contract.message.workflow.WorkflowTimerProcessingPort;
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
import java.rmi.registry.LocateRegistry;
import java.util.List;

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
                invokeRemote(restarted, port);
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
        assertThatThrownBy(() -> new SpringApplicationBuilder(WorkflowServiceApplication.class)
                .environment(environment).run("--spring.profiles.active=prod", "--grab.rpc.enabled=false"))
                .hasStackTraceContaining("WORKFLOW_DB_URL");
    }

    private ConfigurableApplicationContext start(PostgreSQLContainer<?> database, int port) {
        return new SpringApplicationBuilder(WorkflowServiceApplication.class).run(
                "--spring.profiles.active=prod",
                "--WORKFLOW_DB_URL=" + database.getJdbcUrl(),
                "--WORKFLOW_DB_USERNAME=" + database.getUsername(),
                "--WORKFLOW_DB_PASSWORD=" + database.getPassword(),
                "--workflows.datasource.hikari.connection-timeout=500",
                "--grab.rpc.enabled=" + (port > 0),
                "--grab.rpc.registry-port=" + port);
    }

    private void checkRuntime(ConfigurableApplicationContext context) throws Exception {
        assertThat(context).isNotInstanceOf(WebServerApplicationContext.class);
        assertThat(context.getBeansOfType(javax.sql.DataSource.class)).hasSize(1);
        var flyway = context.getBean("workflowsFlyway", Flyway.class);
        flyway.validate();
        assertThat(flyway.info().applied()).filteredOn(migration -> migration.getVersion() != null)
                .hasSize(3);
        var factory = context.getBean("workflowsEntityManagerFactory", EntityManagerFactory.class);
        assertThat(factory.getProperties()).containsEntry("hibernate.hbm2ddl.auto", "validate");

        var scheduler = context.getBean("workflowsJobScheduler", ModuleJobScheduler.class);
        assertThat(scheduler.isRunning()).isTrue();

        var jmx = ManagementFactory.getPlatformMBeanServer();
        assertThat(jmx.isRegistered(new ObjectName("org.springframework.boot:type=Endpoint,name=Health"))).isTrue();
        assertThat(jmx.isRegistered(new ObjectName("org.springframework.boot:type=Endpoint,name=Metrics"))).isTrue();
        var health = context.getBean(HealthEndpoint.class);
        assertThat(health.healthForPath("readiness").getStatus()).isEqualTo(Status.UP);
        assertThat(health.healthForPath("liveness").getStatus()).isEqualTo(Status.UP);
    }

    private void invokeRemote(ConfigurableApplicationContext context, int port) throws Exception {
        var registry = LocateRegistry.getRegistry("127.0.0.1", port);
        List<String> bindings = List.of(registry.list());
        assertThat(bindings).contains(
                WorkflowRuntimeCommandPort.class.getName(),
                WorkflowRuntimeQueryPort.class.getName(),
                WorkflowRuntimeSignalPort.class.getName(),
                WorkflowTimerProcessingPort.class.getName()
        );

        var queryPort = (WorkflowRuntimeQueryPort) registry.lookup(WorkflowRuntimeQueryPort.class.getName());
        var missing = queryPort.getExecution(new CommonId("missing"), "scope");
        assertThat(missing).isNull();
    }
}
