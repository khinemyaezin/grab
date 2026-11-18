package com.grab.service.sellerbff.internal.config;

import com.grab.rpc.adapter.RmiClientFactory;
import com.grab.service.contract.message.account.identity.*;
import com.grab.service.contract.message.account.merchant.*;
import com.grab.service.contract.message.channel.region.RegionPort;
import com.grab.service.contract.message.channel.saleschannel.*;
import com.grab.service.contract.message.merchandising.catalog.*;
import com.grab.service.contract.message.merchandising.inventory.*;
import com.grab.service.contract.message.merchandising.pricing.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SellerRmiClientsConfig {
    @Bean
    AuthenticationPort authenticationPort(RmiClientFactory f) {
        return f.create(AuthenticationPort.class, "account");
    }

    @Bean
    IdentityLookupPort identityLookupPort(RmiClientFactory f) {
        return f.create(IdentityLookupPort.class, "account");
    }

    @Bean
    IdentityAdminPort identityAdminPort(RmiClientFactory f) {
        return f.create(IdentityAdminPort.class, "account");
    }

    @Bean
    RolePermissionPort rolePermissionPort(RmiClientFactory f) {
        return f.create(RolePermissionPort.class, "account");
    }

    @Bean
    AccessAssignmentPort accessAssignmentPort(RmiClientFactory f) {
        return f.create(AccessAssignmentPort.class, "account");
    }

    @Bean
    MerchantAccountPort merchantAccountPort(RmiClientFactory f) {
        return f.create(MerchantAccountPort.class, "account");
    }

    @Bean
    MerchantMembershipPort merchantMembershipPort(RmiClientFactory f) {
        return f.create(MerchantMembershipPort.class, "account");
    }

    @Bean
    MerchantStorefrontPort merchantStorefrontPort(RmiClientFactory f) {
        return f.create(MerchantStorefrontPort.class, "account");
    }

    @Bean
    CatalogCategoryPort catalogCategoryPort(RmiClientFactory f) {
        return f.create(CatalogCategoryPort.class, "merchandising");
    }

    @Bean
    CatalogProductPort catalogProductPort(RmiClientFactory f) {
        return f.create(CatalogProductPort.class, "merchandising");
    }

    @Bean
    CatalogVariantDefinitionPort catalogVariantDefinitionPort(RmiClientFactory f) {
        return f.create(CatalogVariantDefinitionPort.class, "merchandising");
    }

    @Bean
    InventoryAvailabilityPort inventoryAvailabilityPort(RmiClientFactory f) {
        return f.create(InventoryAvailabilityPort.class, "merchandising");
    }

    @Bean
    InventoryFacilityPort inventoryFacilityPort(RmiClientFactory f) {
        return f.create(InventoryFacilityPort.class, "merchandising");
    }

    @Bean
    InventoryItemPort inventoryItemPort(RmiClientFactory f) {
        return f.create(InventoryItemPort.class, "merchandising");
    }

    @Bean
    PriceListPort priceListPort(RmiClientFactory f) {
        return f.create(PriceListPort.class, "merchandising");
    }

    @Bean
    PriceSetPort priceSetPort(RmiClientFactory f) {
        return f.create(PriceSetPort.class, "merchandising");
    }

    @Bean
    PricingQuotePort pricingQuotePort(RmiClientFactory f) {
        return f.create(PricingQuotePort.class, "merchandising");
    }

    @Bean
    RegionPort regionPort(RmiClientFactory f) {
        return f.create(RegionPort.class, "channel");
    }

    @Bean
    SalesChannelManagementPort salesChannelManagementPort(RmiClientFactory f) {
        return f.create(SalesChannelManagementPort.class, "channel");
    }

    @Bean
    PublishableApiKeyPort publishableApiKeyPort(RmiClientFactory f) {
        return f.create(PublishableApiKeyPort.class, "channel");
    }
}
