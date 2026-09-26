package com.fil.week2.service;

import com.fil.week2.dto.CustomerResponse;
import com.fil.week2.exception.CustomerNotFoundException;
import com.fil.week2.model.Customer;
import com.fil.week2.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {
    private CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse suspend(Long customerId) {
        Customer customer = requireCustomer(customerId);
        customer.suspend();
        customerRepository.flush();
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse reinstate(Long customerId) {
        Customer customer = requireCustomer(customerId);
        customer.reinstate();
        customerRepository.flush();
        return CustomerResponse.from(customer);
    }

    private Customer requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAllCustomer() {
        return  customerRepository.findAll()
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }
}
