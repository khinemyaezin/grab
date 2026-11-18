package com.grab.service.merchandising;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceArchitectureTest {

    private static final String PACKAGE = "com.grab.service.merchandising";

    @Test
    void serviceDoesNotDependOnTheCombinedAppOrWebControllers() {
        JavaClasses classes = new ClassFileImporter().importPackages(PACKAGE);
        ArchRule noAppDependencies = noClasses().should().dependOnClassesThat().resideInAPackage("com.grab.app..");
        ArchRule headlessService = noClasses().should().dependOnClassesThat()
                .resideInAPackage("org.springframework.web.bind.annotation..");
        noAppDependencies.check(classes);
        headlessService.check(classes);
    }

    @Test
    void messageAdaptersUseOnlyTheirOwnGroupBuses() {
        JavaClasses classes = new ClassFileImporter().importPackages(PACKAGE);
        String expectedCommandBus = "merchandisingCommandBus";
        String expectedQueryBus = "merchandisingQueryBus";
        for (var javaClass : classes) {
            if (!javaClass.isAnnotatedWith(Component.class)
                    || !javaClass.getPackageName().contains("internal.adapter.inbound.message")) {
                continue;
            }
            for (var constructor : javaClass.reflect().getConstructors()) {
                var parameters = constructor.getParameters();
                for (var parameter : parameters) {
                    if (parameter.getType().equals(CommandBus.class)) {
                        var qualifier = parameter.getAnnotation(Qualifier.class);
                        assertThat(qualifier).isNotNull();
                        assertThat(qualifier.value()).isEqualTo(expectedCommandBus);
                    }
                    if (parameter.getType().equals(QueryBus.class)) {
                        var qualifier = parameter.getAnnotation(Qualifier.class);
                        assertThat(qualifier).isNotNull();
                        assertThat(qualifier.value()).isEqualTo(expectedQueryBus);
                    }
                }
            }
        }
    }
}
