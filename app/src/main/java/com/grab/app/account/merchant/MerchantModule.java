package com.grab.app.account.merchant;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(allowedDependencies = {"shared", "account.identity::port", "channel.region::query", "channel.region::api"})
public class MerchantModule {
}
