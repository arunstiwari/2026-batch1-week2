package com.fil.week2.service;

import com.fil.week2.CustomerRepository;
import com.fil.week2.model.Customer;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class OnboardingService {
    private NotificationSender notificationSender;
    private ExecutorService executor ;
    private CustomerRepository customerRepository;

    public OnboardingService(NotificationSender notificationSender,
                             ExecutorService executor,
                             CustomerRepository customerRepository) {
        this.notificationSender = notificationSender;
        this.executor = executor;
        this.customerRepository = customerRepository;
    }
    public Customer onboard(Customer customer){
        System.out.println("customer:"+customer );
        Customer saved = this.customerRepository.save(customer);
        System.out.println("saved:"+saved);

        executor.submit(()->{
            notificationSender.send(customer.getEmail(),
                    "Welcome "+customer.getName()+" to the system","Your account is ready to be used.");
        });

        return saved;
    }
}
