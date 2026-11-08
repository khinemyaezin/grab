package com.grab.app.checkout.internal.config;

import com.grab.app.checkout.cart.internal.config.CartConfig;
import com.grab.app.checkout.order.config.OrderConfig;
import com.grab.app.checkout.payment.internal.config.PaymentConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        CheckoutDataSourceConfig.class,
        CheckoutBusConfig.class,
        CartConfig.class,
        OrderConfig.class,
        PaymentConfig.class
})
public class CheckoutConfig {
}

