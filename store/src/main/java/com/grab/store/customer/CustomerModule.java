package com.grab.store.customer;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "identity::port"})
public class CustomerModule {
}
