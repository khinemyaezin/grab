package com.grab.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = {
                "com.grab",
                "com.catalog",
                "com.pricing",
                "com.inventory",
                "com.saleschannel"
        },
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_layer_should_not_depend_on_frameworks =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "com.fasterxml.jackson..",
                            "..application..",
                            "..adapter..",
                            "..store.."
                    );

    @ArchTest
    static final ArchRule application_layer_should_not_depend_on_outer_layers =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter.persistence..", "..store..");

    @ArchTest
    static final ArchRule command_service_must_not_access_query_bus_or_query_service =
            noClasses()
                    .that().haveSimpleNameEndingWith("CommandService")
                    .should().dependOnClassesThat()
                    .haveSimpleNameEndingWith("QueryService")
                    .orShould().dependOnClassesThat()
                    .haveSimpleNameEndingWith("QueryBus");

    @ArchTest
    static final ArchRule query_service_must_not_access_command_bus_or_command_service =
            noClasses()
                    .that().haveSimpleNameEndingWith("QueryService")
                    .should().dependOnClassesThat()
                    .haveSimpleNameEndingWith("CommandService")
                    .orShould().dependOnClassesThat()
                    .haveSimpleNameEndingWith("CommandBus");

    @ArchTest
    static final ArchRule controllers_must_not_inject_repositories_or_handlers =
            noClasses()
                    .that().areAnnotatedWith(RestController.class)
                    .should().dependOnClassesThat()
                    .haveSimpleNameEndingWith("Repository")
                    .orShould().dependOnClassesThat()
                    .haveSimpleNameEndingWith("CommandHandler")
                    .orShould().dependOnClassesThat()
                    .haveSimpleNameEndingWith("QueryHandler")
                    .orShould().dependOnClassesThat()
                    .haveSimpleNameEndingWith("UseCase");

    @ArchTest
    static final ArchRule transactional_prohibited_on_controllers_and_services =
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .or().haveSimpleNameEndingWith("Service")
                    .or().haveSimpleNameEndingWith("Mapper")
                    .or().haveSimpleNameEndingWith("Assembler")
                    .or().haveSimpleNameEndingWith("Policy")
                    .should().notBeAnnotatedWith(Transactional.class);

    @ArchTest
    static final ArchRule commands_and_queries_must_be_records =
            classes()
                    .that().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnnotations()
                    .and().haveSimpleNameNotEndingWith("Validator")
                    .and().haveSimpleNameNotEndingWith("Port")
                    .and().haveSimpleNameEndingWith("Command")
                    .or().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnnotations()
                    .and().haveSimpleNameNotEndingWith("Validator")
                    .and().haveSimpleNameNotEndingWith("Port")
                    .and().haveSimpleNameEndingWith("Query")
                    .or().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnnotations()
                    .and().haveSimpleNameNotEndingWith("Validator")
                    .and().haveSimpleNameNotEndingWith("Port")
                    .and().haveSimpleNameEndingWith("Result")
                    .or().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnnotations()
                    .and().haveSimpleNameNotEndingWith("Validator")
                    .and().haveSimpleNameNotEndingWith("Port")
                    .and().resideInAnyPackage("..dto.request..", "..dto.response..")
                    .should().beRecords();

    @ArchTest
    static final ArchRule no_native_queries_in_jpa_repositories =
            methods()
                    .that().areDeclaredInClassesThat().resideInAPackage("..adapter.persistence..")
                    .and().areAnnotatedWith(org.springframework.data.jpa.repository.Query.class)
                    .should(new com.tngtech.archunit.lang.ArchCondition<com.tngtech.archunit.core.domain.JavaMethod>("not have nativeQuery set to true") {
                        @Override
                        public void check(com.tngtech.archunit.core.domain.JavaMethod method, com.tngtech.archunit.lang.ConditionEvents events) {
                            org.springframework.data.jpa.repository.Query queryAnnotation = method.getAnnotationOfType(org.springframework.data.jpa.repository.Query.class);
                            if (queryAnnotation != null && queryAnnotation.nativeQuery()) {
                                events.add(com.tngtech.archunit.lang.SimpleConditionEvent.violated(
                                        method,
                                        "Method " + method.getFullName() + " uses nativeQuery = true which violates R11"
                                ));
                            }
                        }
                    });
}
