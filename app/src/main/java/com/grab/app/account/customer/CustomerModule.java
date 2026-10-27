package com.grab.app.account.customer;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "account.identity::port"})
public class CustomerModule {
}
