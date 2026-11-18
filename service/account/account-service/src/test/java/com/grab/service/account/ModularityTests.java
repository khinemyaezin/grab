package com.grab.service.account;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(AccountServiceApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }
}
