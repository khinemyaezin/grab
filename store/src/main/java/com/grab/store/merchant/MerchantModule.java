package com.grab.store.merchant;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared","identity::port"})
public class MerchantModule {
}
