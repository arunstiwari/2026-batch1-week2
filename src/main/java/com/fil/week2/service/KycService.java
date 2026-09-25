package com.fil.week2.service;

import com.fil.week2.dto.*;
import com.fil.week2.exception.CustomerNotFoundException;
import com.fil.week2.exception.KycNotFoundException;
import com.fil.week2.model.Customer;
import com.fil.week2.model.KycVerification;
import com.fil.week2.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KycService {
    private final CustomerRepository customerRepository;

    public KycService( CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public KycSubmissionResponse submit(Long customerId, KycSubmissionRequest request) {
        Customer customer = validateCustomerExistsAndIfExistReturnCustomer(customerId);
        // Add a method in customer to submit kyc to enforce the business rules
        KycVerification kyc = customer.submitKyc(request.documentType(), request.documentNumber());
        customerRepository.flush();
        return KycSubmissionResponse.from(kyc);
    }

    @Transactional
    public KycResponse approve(Long customerId, KycApprovalRequest request) {
        Customer customer = validateCustomerExistsAndIfExistReturnCustomer(customerId);
        KycVerification kyc = customer.approveKyc(request.reviewedBy());
        customerRepository.flush();
        return KycResponse.from(kyc);
    }

    @Transactional
    public KycResponse reject(Long customerId, KycRejectionRequest request) {
        Customer customer = validateCustomerExistsAndIfExistReturnCustomer(customerId);
        KycVerification kyc = customer.rejectKyc(request.reviewedBy(), request.reason());
        customerRepository.flush();
        return KycResponse.from(kyc);
    }

    public KycResponse findByCustomer(Long customerId) {
        Customer customer = validateCustomerExistsAndIfExistReturnCustomer(customerId);
        KycVerification kyc = customer.getKyc();
        if (kyc == null) {
            throw new KycNotFoundException(customerId);
        }
        return KycResponse.from(kyc);
    }

    private Customer validateCustomerExistsAndIfExistReturnCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with id " + customerId + " does not exist in the system"));
    }




}
