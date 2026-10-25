package com.grab;

import com.grab.app.EcommerceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationModularityTests {
    ApplicationModules modules = ApplicationModules.of(EcommerceApplication.class);

    @Test
    void discoversTheFourClustersAndOpenSharedModule() {
        for (String moduleName : List.of(
                "productmerchandising", "ordermanagement", "identityaccount", "channeldiscovery")) {
            assertThat(modules.getModuleByName(moduleName)).as("module %s", moduleName).isPresent();
        }

        ApplicationModule shared = modules.getModuleByName("shared").orElseThrow()
                .getBasePackage().getAnnotation(ApplicationModule.class).orElseThrow();
        assertThat(shared.type()).isEqualTo(ApplicationModule.Type.OPEN);
    }

    @Test
    void exposesOwnerQualifiedNamedInterfaces() {
        assertNamedInterface("productmerchandising", "catalog-query");
        assertNamedInterface("productmerchandising", "catalog-api");
        assertNamedInterface("productmerchandising", "catalog-representation");
        assertNamedInterface("productmerchandising", "pricing-query");
        assertNamedInterface("productmerchandising", "inventory-query");
        assertNamedInterface("ordermanagement", "workflow-api");
        assertNamedInterface("channeldiscovery", "saleschannel-query");
        assertNamedInterface("channeldiscovery", "region-query");
    }

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }

    private void assertNamedInterface(String moduleName, String interfaceName) {
        assertThat(modules.getModuleByName(moduleName).orElseThrow()
                .getNamedInterfaces().getByName(interfaceName))
                .as("%s::%s", moduleName, interfaceName)
                .isPresent();
    }
}
