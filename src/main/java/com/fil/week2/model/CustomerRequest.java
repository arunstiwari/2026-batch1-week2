package com.fil.week2.model;

import java.util.Set;

public record CustomerRequest(Long id,
                              String name,
                              String email,
                              Address address,
                              Address billingAdress,
                              CustomerStatus status,
                              Set<String> tags) {
    public static Customer from(CustomerRequest customer) {
       Customer customer1 = new Customer(customer.id(), customer.name(), customer.email());
       customer1.setAddress(customer.address());
       customer1.setBillingAddress(customer.billingAdress());
       customer1.setStatus(customer.status());
       customer1.setTags(customer.tags());
        return customer1;
    }
}
