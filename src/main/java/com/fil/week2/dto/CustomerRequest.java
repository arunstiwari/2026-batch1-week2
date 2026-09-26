package com.fil.week2.dto;

import com.fil.week2.model.Address;
import com.fil.week2.model.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

/**
 * Carries neither an id nor a status: identity is the database's to issue, and
 * standing is the domain's to decide.
 */
public record CustomerRequest(@NotBlank String name,
                              @NotBlank @Email String email,
                              Address address,
                              Address billingAddress,
                              Set<String> tags) {

    public Customer toCustomer() {
        Customer customer = new Customer(name, email);
        customer.setAddress(address);
        customer.setBillingAddress(billingAddress);
        if (tags != null) {
            customer.setTags(tags);
        }
        return customer;
    }
}
