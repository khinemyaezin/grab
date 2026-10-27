package com.grab;

import com.grab.app.EcommerceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ModularityTests {
    ApplicationModules modules = ApplicationModules.of(EcommerceApplication.class);

    @Test
    void verifiesModularStructure() {
        Stream.of(
                        "com.grab.app.catalog.catalog",
                        "com.grab.app.catalog.inventory",
                        "com.grab.app.catalog.pricing",
                        "com.grab.app.order.cart",
                        "com.grab.app.account.identity",
                        "com.grab.app.account.merchant",
                        "com.grab.app.account.customer",
                        "com.grab.app.channel.saleschannel",
                        "com.grab.app.channel.region",
                        "com.grab.app.channel.discovery",
                        "com.grab.app.workflows",
                        "com.grab.app.platform.seller",
                        "com.grab.app.platform.marketplace"
                )
                .forEach(name -> assertThat(modules.getModuleByName(name)).isPresent());
    }

    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
