package com.fil.week2.service;

import com.fil.week2.dto.*;
import com.fil.week2.exception.CustomerNotFoundException;
import com.fil.week2.exception.KycNotFoundException;
import com.fil.week2.model.Customer;
import com.fil.week2.model.DocumentNumber;
import com.fil.week2.model.KycVerification;
import com.fil.week2.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KycService {
    private final CustomerRepository customerRepository;

    public KycService( CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public KycSubmissionResponse submit(Long customerId, KycSubmissionRequest request) {
        Customer customer = requireCustomer(customerId);
        // Add a method in customer to submit kyc to enforce the business rules
        KycVerification kyc = customer.submitKyc(request.documentType(), DocumentNumber.of(request.documentNumber()));
        customerRepository.flush();
        return KycSubmissionResponse.from(kyc);
    }

    @Transactional
    public KycResponse approve(Long customerId, KycApprovalRequest request) {
        Customer customer = requireCustomer(customerId);
        KycVerification kyc = customer.approveKyc(request.reviewedBy());
        customerRepository.flush();
        return KycResponse.from(kyc);
    }

    @Transactional
    public KycResponse reject(Long customerId, KycRejectionRequest request) {
        Customer customer = requireCustomer(customerId);
        KycVerification kyc = customer.rejectKyc(request.reviewedBy(), request.reason());
        customerRepository.flush();
        return KycResponse.from(kyc);
    }

    @Transactional(readOnly = true)
    public KycResponse findByCustomer(Long customerId) {
        return KycResponse.from(requireCustomer(customerId).currentVerification()
                .orElseThrow(() -> new KycNotFoundException(customerId)));
    }

    /** Oldest first, so a customer's verification history reads as a narrative. */
    @Transactional(readOnly = true)
    public List<KycResponse> findHistory(Long customerId) {
        Customer customer = requireCustomer(customerId);
        if (customer.getKycVerifications().isEmpty()) {
            throw new KycNotFoundException(customerId);
        }
        return customer.getKycVerifications().stream().map(KycResponse::from).toList();
    }

    private Customer requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }




}
