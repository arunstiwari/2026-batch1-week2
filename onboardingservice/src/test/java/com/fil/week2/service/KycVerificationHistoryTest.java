package com.fil.week2.service;

import com.fil.week2.dto.AccountOpenRequest;
import com.fil.week2.exception.InvalidStateTransitionException;
import com.fil.week2.exception.KycNotFoundException;
import com.fil.week2.model.AccountType;
import com.fil.week2.model.Customer;
import com.fil.week2.model.DocumentNumber;
import com.fil.week2.model.DocumentType;
import com.fil.week2.model.KycStatus;
import com.fil.week2.model.KycVerification;
import com.fil.week2.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * A Customer accumulates verifications instead of overwriting one. Two facts fall
 * out: the record of a refusal survives the next attempt, and an approved
 * customer can refresh an expiring document without losing their access.
 */
@SpringBootTest
@Transactional
class KycVerificationHistoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    AccountService accountService;

    private Customer saved(String email) {
        Customer customer = new Customer("Verified Person", email);
        customerRepository.save(customer);
        return customer;
    }

    @Test
    void anApprovedCustomerCanReVerify() {
        Customer customer = saved("reverify@example.com");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P100000001"));
        customer.approveKyc("compliance-officer");
        customerRepository.flush();

        assertThatNoException().isThrownBy(() ->
                customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P200000002")));
    }

    /** The finding this phase exists for: a refusal used to be erased on resubmission. */
    @Test
    void aRejectionSurvivesTheNextSubmission() {
        Customer customer = saved("rejection-kept@example.com");
        customer.submitKyc(DocumentType.NATIONAL_ID, DocumentNumber.of("N100000001"));
        customer.rejectKyc("compliance-officer", "Document illegible");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P300000003"));
        customerRepository.flush();

        assertThat(customer.getKycVerifications()).hasSize(2);
        assertThat(customer.getKycVerifications().get(0))
                .extracting(KycVerification::getStatus,
                        KycVerification::getReviewedBy,
                        KycVerification::getRejectionReason)
                .containsExactly(KycStatus.REJECTED, "compliance-officer", "Document illegible");
        assertThat(customer.getKycVerifications().get(1).getStatus()).isEqualTo(KycStatus.SUBMITTED);
    }

    /** Replaces the guard that resubmission-from-REJECTED used to provide. */
    @Test
    void onlyOneVerificationMayAwaitADecision() {
        Customer customer = saved("one-open@example.com");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P400000004"));

        assertThatExceptionOfType(InvalidStateTransitionException.class)
                .isThrownBy(() -> customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P500000005")))
                .withMessageContaining("awaiting a decision");
    }

    @Test
    void aRefreshAwaitingReviewDoesNotRevokeTheStandingApproval() {
        Customer customer = saved("refresh@example.com");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P600000006"));
        customer.approveKyc("compliance-officer");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P700000007"));
        customerRepository.flush();

        assertThat(customer.currentVerification()).get()
                .extracting(KycVerification::getStatus).isEqualTo(KycStatus.SUBMITTED);
        assertThat(customer.standingVerification()).get()
                .extracting(KycVerification::getStatus).isEqualTo(KycStatus.APPROVED);
        assertThat(customer.isVerified()).isTrue();

        assertThatNoException().isThrownBy(() ->
                accountService.openAccount(customer.getId(), new AccountOpenRequest(AccountType.SAVINGS)));
    }

    @Test
    void aRejectionAfterAnApprovalRevokesIt() {
        Customer customer = saved("revoked@example.com");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P800000008"));
        customer.approveKyc("compliance-officer");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("P900000009"));
        customer.rejectKyc("compliance-officer", "Photograph does not match");
        customerRepository.flush();

        assertThat(customer.isVerified()).isFalse();
    }

    @Test
    void neverSubmittedIsAMissingRecordNotAnIllegalTransition() {
        Customer customer = saved("never@example.com");

        assertThatExceptionOfType(KycNotFoundException.class)
                .isThrownBy(() -> customer.approveKyc("compliance-officer"));
    }

    @Test
    void decidingTwiceIsAnIllegalTransitionNotAMissingRecord() {
        Customer customer = saved("twice@example.com");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("PA00000010"));
        customer.approveKyc("compliance-officer");
        customerRepository.flush();

        assertThatExceptionOfType(InvalidStateTransitionException.class)
                .isThrownBy(() -> customer.approveKyc("compliance-officer"));
    }

    @Test
    void theHistoryIsOrderedOldestFirst() {
        Customer customer = saved("ordered@example.com");
        customer.submitKyc(DocumentType.NATIONAL_ID, DocumentNumber.of("NB00000011"));
        customer.rejectKyc("officer-one", "First refusal");
        customer.submitKyc(DocumentType.DRIVING_LICENCE, DocumentNumber.of("DC00000012"));
        customer.rejectKyc("officer-two", "Second refusal");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("PD00000013"));
        customer.approveKyc("officer-three");
        customerRepository.flush();

        assertThat(customer.getKycVerifications())
                .extracting(KycVerification::getRejectionReason)
                .containsExactly("First refusal", "Second refusal", null);
    }

    @Test
    void theHistoryCannotBeMutatedThroughTheGetter() {
        Customer customer = saved("immutable@example.com");
        customer.submitKyc(DocumentType.PASSPORT, DocumentNumber.of("PE00000014"));

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> customer.getKycVerifications().clear());
    }
}
