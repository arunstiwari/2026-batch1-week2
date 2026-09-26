package com.fil.week2.dto;

import com.fil.week2.model.Address;
import com.fil.week2.model.Customer;
import com.fil.week2.model.Standing;

import java.time.Instant;
import java.util.Set;

public record CustomerResponse(Long id, String name,
                               String email,
                               Address address,
                               Address billingAddress,
                               Standing standing,
                               long version,
                               Set<String> tags,
                               Instant createdAt,
                               Instant updatedAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(),
                customer.getName(), customer.getEmail(),
                customer.getAddress(), customer.getBillingAddress(),
                customer.getStanding(), customer.getVersion(),
                customer.getTags(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
