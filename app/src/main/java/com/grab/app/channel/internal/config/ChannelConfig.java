package com.grab.app.channel.internal.config;

import com.grab.app.channel.discovery.internal.config.DiscoveryConfig;
import com.grab.app.channel.region.internal.config.RegionConfig;
import com.grab.app.channel.saleschannel.internal.config.SalesChannelConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        ChannelDataSourceConfig.class,
        ChannelBusConfig.class,
        DiscoveryConfig.class,
        RegionConfig.class,
        SalesChannelConfig.class
})
public class ChannelConfig {
}

