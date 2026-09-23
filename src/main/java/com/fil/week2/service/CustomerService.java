package com.fil.week2.service;

import com.fil.week2.repository.CustomerRepository;
import com.fil.week2.dto.CustomerResponse;
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

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAllCustomer() {
        return  customerRepository.findAll()
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }
}
