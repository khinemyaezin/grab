package com.grab.store.customer.internal.config;

import com.customer.application.port.inbound.AttachUserToCustomerUseCase;
import com.customer.application.port.inbound.CreateGuestCustomerUseCase;
import com.customer.application.port.inbound.GetCustomerByIdUseCase;
import com.customer.application.port.inbound.GetCustomerByUserIdUseCase;
import com.customer.application.port.inbound.RegisterCustomerUseCase;
import com.customer.application.port.inbound.SuspendCustomerUseCase;
import com.customer.application.port.outbound.CustomerQueryPort;
import com.customer.application.service.AttachUserToCustomerService;
import com.customer.application.service.CreateGuestCustomerService;
import com.customer.application.service.GetCustomerByIdService;
import com.customer.application.service.GetCustomerByUserIdService;
import com.customer.application.service.RegisterCustomerService;
import com.customer.application.service.SuspendCustomerService;
import com.customer.domain.port.outbound.CustomerRepository;
import com.grab.framework.id.IdGenerator;
import com.grab.store.customer.internal.adapter.StoreCustomerQueryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@CustomerEnabled
public class CustomerUseCaseConfig {

    @Bean
    public RegisterCustomerUseCase registerCustomerUseCase(
            CustomerRepository customers,
            IdGenerator ids
    ) {
        return new RegisterCustomerService(customers, ids);
    }

    @Bean
    public CreateGuestCustomerUseCase createGuestCustomerUseCase(
            CustomerRepository customers,
            IdGenerator ids
    ) {
        return new CreateGuestCustomerService(customers, ids);
    }

    @Bean
    public AttachUserToCustomerUseCase attachUserToCustomerUseCase(CustomerRepository customers) {
        return new AttachUserToCustomerService(customers);
    }

    @Bean
    public SuspendCustomerUseCase suspendCustomerUseCase(CustomerRepository customers) {
        return new SuspendCustomerService(customers);
    }

    @Bean
    public GetCustomerByIdUseCase getCustomerByIdUseCase(CustomerQueryPort customerQueryPort) {
        return new GetCustomerByIdService(customerQueryPort);
    }

    @Bean
    public GetCustomerByUserIdUseCase getCustomerByUserIdUseCase(CustomerQueryPort customerQueryPort) {
        return new GetCustomerByUserIdService(customerQueryPort);
    }

    @Bean
    public CustomerQueryPort storeCustomerQueryPort(
            CustomerQueryPort customerQueryPort
    ) {
        return new StoreCustomerQueryAdapter(customerQueryPort);
    }
}
