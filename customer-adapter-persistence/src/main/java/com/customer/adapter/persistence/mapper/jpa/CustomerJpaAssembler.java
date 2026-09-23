package com.customer.adapter.persistence.mapper.jpa;

import com.customer.adapter.persistence.entity.CustomerAddressEntity;
import com.customer.adapter.persistence.entity.CustomerEntity;
import com.customer.domain.aggregate.Customer;
import com.customer.domain.entity.CustomerAddress;
import com.customer.domain.enums.CustomerStatus;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CustomerJpaAssembler {
    private final IdMapper ids;

    public Customer toDomain(CustomerEntity entity) {
        List<CustomerAddress> addresses = entity.getAddresses() == null ? List.of() :
                entity.getAddresses().stream().map(this::toAddressDomain).toList();
        return new Customer(
                ids.map(entity.getUuid()),
                entity.getUserId() == null ? null : ids.map(entity.getUserId()),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getStatus(),
                new ArrayList<>(addresses),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CustomerEntity toEntity(Customer customer, CustomerEntity existing) {
        CustomerEntity entity = existing == null ? new CustomerEntity() : existing;
        if (existing == null) {
            entity.setUuid(customer.getId().getValue());
            entity.setCreatedAt(customer.getCreatedAt());
        }
        entity.setUserId(customer.getUserIdOptional().map(Id::getValue).orElse(null));
        entity.setEmail(customer.getEmail());
        entity.setDisplayName(customer.getDisplayName());
        entity.setStatus(customer.getStatus());
        entity.setUpdatedAt(customer.getUpdatedAt());
        return entity;
    }

    private CustomerAddress toAddressDomain(CustomerAddressEntity entity) {
        return new CustomerAddress(
                ids.map(entity.getUuid()),
                entity.getLine1(),
                entity.getCity(),
                entity.getCountry(),
                entity.getPhone(),
                entity.isDefaultAddress()
        );
    }
}
