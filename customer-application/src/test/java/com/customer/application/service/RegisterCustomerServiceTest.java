package com.customer.application.service;

import com.customer.application.exception.CustomerServiceException;
import com.customer.application.model.write.CustomerResult;
import com.customer.application.model.write.RegisterCustomerCommand;
import com.customer.application.port.outbound.UserProfileQueryPort;
import com.customer.domain.aggregate.Customer;
import com.customer.domain.port.outbound.CustomerRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerServiceTest {

    @Mock
    private CustomerRepository customers;

    @Mock
    private IdGenerator ids;

    @Mock
    private UserProfileQueryPort userProfileQuery;

    private RegisterCustomerService service;

    @BeforeEach
    void setUp() {
        service = new RegisterCustomerService(customers, ids, userProfileQuery);
    }

    @Test
    void shouldRegisterCustomer_whenPlatformIsCustomerApp() {
        Id userId = () -> "user-123";
        Id customerId = () -> "cust-456";
        RegisterCustomerCommand command = new RegisterCustomerCommand(userId, "test@example.com", "Test User");

        UserProfileQueryPort.UserProfileResponse profile = new UserProfileQueryPort.UserProfileResponse(
                "user-123", "test@example.com", "ACTIVE", "2023-01-01T00:00:00Z",
                List.of("CUSTOMER_APP")
        );

        when(userProfileQuery.getUserProfile(userId)).thenReturn(profile);
        when(customers.findByUserId(userId)).thenReturn(Optional.empty());
        when(ids.generateId()).thenReturn(customerId);
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResult result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.customer().id()).isEqualTo("cust-456");
        assertThat(result.customer().userId()).isEqualTo("user-123");
        assertThat(result.customer().email()).isEqualTo("test@example.com");

        verify(customers).save(any(Customer.class));
    }

    @Test
    void shouldReturnNull_whenPlatformIsNotCustomerApp() {
        Id userId = () -> "user-123";
        RegisterCustomerCommand command = new RegisterCustomerCommand(userId, "test@example.com", "Test User");

        UserProfileQueryPort.UserProfileResponse profile = new UserProfileQueryPort.UserProfileResponse(
                "user-123", "test@example.com", "ACTIVE", "2023-01-01T00:00:00Z",
                List.of("SELLER_PORTAL")
        );

        when(userProfileQuery.getUserProfile(userId)).thenReturn(profile);

        CustomerResult result = service.execute(command);

        assertThat(result).isNull();
        verify(customers, never()).save(any(Customer.class));
    }

    @Test
    void shouldReturnNull_whenProfileIsNull() {
        Id userId = () -> "user-123";
        RegisterCustomerCommand command = new RegisterCustomerCommand(userId, "test@example.com", "Test User");

        when(userProfileQuery.getUserProfile(userId)).thenReturn(null);

        CustomerResult result = service.execute(command);

        assertThat(result).isNull();
        verify(customers, never()).save(any(Customer.class));
    }

    @Test
    void shouldThrowException_whenCustomerAlreadyExists() {
        Id userId = () -> "user-123";
        RegisterCustomerCommand command = new RegisterCustomerCommand(userId, "test@example.com", "Test User");

        UserProfileQueryPort.UserProfileResponse profile = new UserProfileQueryPort.UserProfileResponse(
                "user-123", "test@example.com", "ACTIVE", "2023-01-01T00:00:00Z",
                List.of("CUSTOMER_APP")
        );

        Customer existingCustomer = Customer.register(
                () -> "cust-existing", userId, "test@example.com", "Test User", java.time.Instant.now()
        );

        when(userProfileQuery.getUserProfile(userId)).thenReturn(profile);
        when(customers.findByUserId(userId)).thenReturn(Optional.of(existingCustomer));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CustomerServiceException.class)
                .hasMessageContaining("Customer already exists for user");

        verify(customers, never()).save(any(Customer.class));
    }
}
