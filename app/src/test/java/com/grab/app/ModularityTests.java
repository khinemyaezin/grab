package com.grab.app;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;


public class ModularityTests {
    ApplicationModules modules = ApplicationModules.of(Application.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }
}
