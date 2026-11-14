package com.grab.app.architecture;

import com.grab.framework.scheduling.ClusterSingletonJob;
import com.grab.framework.scheduling.CompetingWorkerJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.ObjectInputStream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Scheduling plan 3.6 and 1.8: every scheduled job is explicitly classified, singleton jobs
 * actually go through a cluster lock, and no production code deserializes Java object streams.
 */
class SchedulingArchitectureTest {

    private static final String[] PRODUCTION_PACKAGES = {
            "com.grab", "com.cart", "com.catalog", "com.customer", "com.identity", "com.inventory",
            "com.manifest", "com.merchant", "com.pricing", "com.region", "com.saleschannel"
    };

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(PRODUCTION_PACKAGES);
    }

    @Test
    void everyScheduledMethodIsClassified() {
        methods()
                .that().areAnnotatedWith(Scheduled.class)
                .should(beClassifiedExactlyOnce())
                .because("each @Scheduled job must declare whether it is a competing worker (class A) "
                        + "or a cluster singleton guarded by a lock (class B)")
                .check(classes);
    }

    @Test
    void eventListenersAreNotAsync() {
        methods()
                .that().areAnnotatedWith(EventListener.class)
                .should().notBeAnnotatedWith(Async.class)
                .because("outbox-delivered events must be handled on the delivery thread so failures are retried "
                        + "by the outbox and the inbox insert shares the handler transaction (plan P6)")
                .check(classes);
    }

    @Test
    void onlyTheBootstrapRunnerReactsToApplicationReady() {
        noClasses()
                .that().resideOutsideOfPackage("com.grab.app.shared.bootstrap..")
                .should().dependOnClassesThat().areAssignableTo(ApplicationReadyEvent.class)
                .because("boot-time work must be a BootstrapTask (cluster lock, retries, readiness gating), "
                        + "not an ad-hoc ApplicationReadyEvent listener (plan 3.6)")
                .check(classes);
    }

    @Test
    void clusterSingletonJobsUseASingletonJobExecutor() {
        methods()
                .that().areAnnotatedWith(ClusterSingletonJob.class)
                .should(beDeclaredInClassHoldingSingletonJobExecutor())
                .check(classes);
    }

    @Test
    void productionCodeNeverUsesJavaDeserialization() {
        noClasses()
                .should().dependOnClassesThat().areAssignableTo(ObjectInputStream.class)
                .because("Java deserialization of stored payloads is a remote-code-execution risk")
                .check(classes);
    }

    private static ArchCondition<JavaMethod> beClassifiedExactlyOnce() {
        return new ArchCondition<>("be annotated with exactly one of @ClusterSingletonJob or @CompetingWorkerJob") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                boolean singleton = method.isAnnotatedWith(ClusterSingletonJob.class);
                boolean competing = method.isAnnotatedWith(CompetingWorkerJob.class);
                boolean satisfied = singleton ^ competing;
                String message = method.getFullName() + (satisfied ? " is classified" : " is not classified exactly once");
                events.add(new SimpleConditionEvent(method, satisfied, message));
            }
        };
    }

    private static ArchCondition<JavaMethod> beDeclaredInClassHoldingSingletonJobExecutor() {
        DescribedPredicate<JavaClass> holdsExecutor = DescribedPredicate.describe(
                "holds a SingletonJobExecutor",
                javaClass -> javaClass.getAllFields().stream()
                        .anyMatch(field -> field.getRawType().isAssignableTo(SingletonJobExecutor.class)));
        return new ArchCondition<>("be declared in a class that " + holdsExecutor.getDescription()) {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                JavaClass owner = method.getOwner();
                boolean satisfied = holdsExecutor.test(owner);
                String message = method.getFullName() + (satisfied ? " uses" : " does not use") + " a SingletonJobExecutor";
                events.add(new SimpleConditionEvent(method, satisfied, message));
            }
        };
    }
}
