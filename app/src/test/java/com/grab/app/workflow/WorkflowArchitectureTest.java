package com.grab.app.workflow;

import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Modifier;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowArchitectureTest {

    private final JavaClasses workflow = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.grab.framework.workflow", "com.grab.workflow.infrastructure", "com.grab.app.workflow");

    @Test
    void workflow_dependenciesStayWithinTheirLayer() {
        noClasses().that().resideInAPackage("com.grab.framework.workflow..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..",
                        "com.fasterxml.jackson..", "com.grab.workflow.infrastructure..", "com.grab.app..")
                .check(workflow);
        noClasses().that().resideInAPackage("com.grab.app.workflow..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web.bind.annotation..",
                        "com.grab.workflow.infrastructure.entity..", "com.grab.workflow.infrastructure.repository..")
                .check(workflow);
        noClasses().that().resideInAnyPackage("..workflow.internal.command..", "..workflow.internal.service..")
                .should().dependOnClassesThat().haveSimpleName("QueryBus")
                .check(workflow);
        noClasses().that().resideInAPackage("..workflow.internal.query..")
                .should().dependOnClassesThat().haveSimpleName("CommandBus")
                .check(workflow);
    }

    @Test
    void workflow_transactionsUseProxyableBoundariesAndNoNativeQueries() {
        workflow.forEach(type -> {
            Class<?> owner = type.reflect();
            type.getMethods().forEach(method -> {
                if (method.isAnnotatedWith(Query.class)) {
                    Query query = method.getAnnotationOfType(Query.class);
                    assertFalse(query.nativeQuery(), method.getFullName());
                }
                boolean transactional = method.isAnnotatedWith(WorkflowTransactional.class)
                        || method.isAnnotatedWith(Transactional.class)
                        || method.isMetaAnnotatedWith(Transactional.class);
                if (transactional) {
                    String ownerPackage = owner.getPackageName();
                    assertTrue(ownerPackage.endsWith(".handler") || ownerPackage.contains(".adapter."),
                            method.getFullName());
                    assertFalse(Modifier.isFinal(owner.getModifiers()), owner.getName());
                }
            });
        });
    }
}
