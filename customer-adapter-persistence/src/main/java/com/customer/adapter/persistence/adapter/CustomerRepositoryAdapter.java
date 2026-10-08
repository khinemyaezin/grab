package com.customer.adapter.persistence.adapter;

import com.customer.adapter.persistence.entity.CustomerEntity;
import com.customer.adapter.persistence.mapper.jpa.CustomerJpaAssembler;
import com.customer.adapter.persistence.repository.jpa.CustomerJpaRepository;
import com.customer.domain.aggregate.Customer;
import com.customer.domain.port.outbound.CustomerRepository;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {
    private final CustomerJpaRepository customers;
    private final CustomerJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Customer> findById(Id id) {
        return executor.query("Customer", () -> customers.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public Optional<Customer> findByUserId(Id userId) {
        return executor.query("Customer", () -> customers.findByUserId(userId.getValue()).map(assembler::toDomain));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return executor.query("Customer", () -> customers.findByEmail(email).map(assembler::toDomain));
    }

    @Override
    public Customer save(Customer customer) {
        return executor.command("Customer", () -> {
            CustomerEntity existing = customers.findByUuid(customer.getId().getValue()).orElse(null);
            CustomerEntity saved = customers.save(assembler.toEntity(customer, existing));
            List<Event> pending = customer.pullEvents();
            events.produce("Customer", customer.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }
}
