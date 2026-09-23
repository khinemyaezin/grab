package com.customer.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetCustomerByIdQuery(Id customerId) implements Query<CustomerView> {
}
