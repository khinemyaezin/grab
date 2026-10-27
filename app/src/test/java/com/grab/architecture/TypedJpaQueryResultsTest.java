package com.grab.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class TypedJpaQueryResultsTest {
    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(
                    "com.catalog.adapter.persistence", "com.inventory.adapter.persistence",
                    "com.grab.discovery.adapter.persistence", "com.pricing.adapter.persistence",
                    "com.saleschannel.adapter.persistence", "com.cart.adapter.persistence",
                    "com.region.adapter.persistence", "com.customer.adapter.persistence",
                    "com.identity.adapter.persistence", "com.merchant.adapter.persistence",
                    "com.manifest.adapter.persistence",
                "com.grab.workflow.infrastructure", "com.grab.app.channel.discovery.internal.adapter.inbound.event",
                "com.grab.app.catalog.inventory.internal.adapter.inbound.event"
            );

    @Test
    void repositoryQuery_allRepositories_returnsTypedResults() {
        for (JavaClass candidate : classes) {
            if (!candidate.isAssignableTo(JpaRepository.class)) {
                continue;
            }
            Class<?> repository = candidate.reflect();
            for (Method method : repository.getDeclaredMethods()) {
                Type resultType = method.getGenericReturnType();
                boolean untyped = containsUntypedObject(resultType);
                assertThat(untyped).as("Typed JPA result: %s", method).isFalse();
            }
        }
    }

    @Test
    void persistenceRecords_allAdapters_declaresOnlyInfrastructureErrors() {
        for (JavaClass candidate : classes) {
            String packageName = candidate.getPackageName();
            if (!packageName.contains(".adapter.persistence") || packageName.contains(".exception")) {
                continue;
            }
            Class<?> type = candidate.reflect();
            assertThat(type.isRecord())
                    .as("Read views and projection records belong to inner modules: %s", type.getName())
                    .isFalse();
        }
    }

    @Test
    void projectionEventListener_allListeners_dependsOnCommandsWithoutPersistence() {
        noClasses().that().haveSimpleName("ProductVariantViewProjectionEventListener")
                .or().haveSimpleName("BuyableOfferProjectionEventListener")
                .should().dependOnClassesThat().resideInAnyPackage("..adapter.persistence..")
                .check(classes);
    }

    private boolean containsUntypedObject(Type type) {
        if (type instanceof Class<?> resultClass) {
            if (resultClass.isArray()) {
                Class<?> component = resultClass.getComponentType();
                return containsUntypedObject(component);
            }
            return resultClass == Object.class;
        }
        if (type instanceof ParameterizedType parameterized) {
            for (Type argument : parameterized.getActualTypeArguments()) {
                if (containsUntypedObject(argument)) {
                    return true;
                }
            }
        }
        if (type instanceof GenericArrayType array) {
            Type component = array.getGenericComponentType();
            return containsUntypedObject(component);
        }
        return false;
    }
}
