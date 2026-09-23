package com.fil.week2.repository;

import com.fil.week2.model.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
//    @Override
//    @EntityGraph(attributePaths = "tags")
//    List<Customer> findAll();
}
