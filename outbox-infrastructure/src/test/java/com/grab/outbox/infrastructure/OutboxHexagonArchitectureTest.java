package com.grab.outbox.infrastructure;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class OutboxHexagonArchitectureTest {

    private static final String ROOT = "com.grab.outbox.infrastructure.";
    private static final String CORE = ROOT + "core..";
    private static final String SERVICES = ROOT + "core.service..";
    private static final String ADAPTERS = ROOT + "adapter..";

    @Test
    void dependenciesPointIntoFrameworkFreeCore() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT.substring(0, ROOT.length() - 1));

        noClasses().that().resideInAPackage(CORE).should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..", "jakarta.persistence..", "org.hibernate..", "io.micrometer..", "net.javacrumbs..",
                ADAPTERS, ROOT + "module..", ROOT + "inbox..", ROOT + "workflow..").check(classes);
        noClasses().that().resideOutsideOfPackages(ROOT + "module..", CORE)
                .should().dependOnClassesThat().resideInAPackage(SERVICES).check(classes);
        noClasses().that().resideOutsideOfPackage(ROOT + "adapter.out.jpa..")
                .should().dependOnClassesThat().haveFullyQualifiedName("jakarta.persistence.LockModeType").check(classes);
        noClasses().should().dependOnClassesThat().resideInAnyPackage(
                "com.grab.scheduling.infrastructure..", "com.grab.app..", "com.grab.store..", "com.grab.workflow..",
                "com.catalog..", "com.inventory..", "com.cart..", "com.customer..", "com.identity..", "com.merchant..",
                "com.pricing..", "com.region..", "com.saleschannel..").check(classes);
    }
}
