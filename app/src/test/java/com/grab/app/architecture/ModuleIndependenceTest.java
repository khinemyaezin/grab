package com.grab.app.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ModuleIndependenceTest {

    private static final Map<String, String> OWNERS = Map.of(
            "cart", "checkout.cart",
            "catalog", "merchandising.catalog",
            "customer", "account.customer",
            "identity", "account.identity",
            "inventory", "merchandising.inventory",
            "merchant", "account.merchant",
            "pricing", "merchandising.pricing",
            "region", "channel.region",
            "saleschannel", "channel.saleschannel");

    @Test
    void eachOutboxConfigDependsOnItsModuleAndInfrastructureLibraries() {
        var imported = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.grab.app", "com.grab.workflow.infrastructure");

        OWNERS.forEach((owner, module) -> {
            String ownModule = "com.grab.app." + module + ".";
            classes().that().resideInAPackage(ownModule + "internal.config")
                    .and().haveSimpleNameEndingWith("ModuleOutboxConfig")
                    .should().onlyDependOnClassesThat(allowed(ownModule, "com." + owner + "."))
                    .check(imported);
        });
        classes().that().haveFullyQualifiedName("com.grab.workflow.infrastructure.config.WorkflowInfraConfig")
                .should().onlyDependOnClassesThat(allowed("com.grab.workflow.infrastructure.", "org.slf4j."))
                .check(imported);
    }

    private static DescribedPredicate<JavaClass> allowed(String owningPackage, String ownerArtifact) {
        return DescribedPredicate.describe("belongs to the owning module or a platform library", dependency -> {
            String name = dependency.getPackageName() + ".";
            return name.startsWith(owningPackage) || name.startsWith(ownerArtifact)
                    || name.startsWith("com.grab.framework.") || name.startsWith("com.grab.outbox.infrastructure.")
                    || name.startsWith("com.grab.scheduling.infrastructure.")
                    || name.startsWith("org.springframework.") || name.startsWith("io.micrometer.")
                    || name.startsWith("jakarta.persistence.") || name.startsWith("java.") || name.startsWith("javax.sql.");
        });
    }
}
