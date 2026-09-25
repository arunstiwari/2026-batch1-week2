package com.fil.week2.model;

import com.fil.week2.exception.InvalidStateTransitionException;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "kyc_verification")
public class KycVerification extends AuditableEntity{
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 40)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus status;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason", length = 300)
    private String rejectionReason;

    @Version
    private long version;

    protected KycVerification() {}

    public KycVerification(Customer customer, DocumentType documentType, String documentNumber){
        this.customer = customer;
        this.documentType = requireDocumentType(documentType);
        this.documentNumber = documentNumber;
        this.status = KycStatus.SUBMITTED;
    }

    private static DocumentType requireDocumentType(DocumentType documentType) {
        if (documentType == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        return documentType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public KycStatus getStatus() {
        return status;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "KycVerification{" +
                "id=" + id +
                ", customer=" + customer +
                ", documentType=" + documentType +
                ", documentNumber='" + documentNumber + '\'' +
                ", status=" + status +
                ", reviewedBy='" + reviewedBy + '\'' +
                ", reviewedAt=" + reviewedAt +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", version=" + version +
                '}';
    }

    public void resubmit(DocumentType documentType, String documentNumber) {
        if (status != KycStatus.REJECTED) {
            throw new InvalidStateTransitionException(
                    "KYC for customer " + id + " is already " + status + " and cannot be submitted again");
        }
        this.documentType = requireDocumentType(documentType);
        this.documentNumber = documentNumber;
        this.status = KycStatus.SUBMITTED;
        this.reviewedBy = null;
        this.reviewedAt = null;
        this.rejectionReason = null;
    }

    public void approve(String reviewedBy) {
        checkEligibilityForSubmission("approved");
        this.status = KycStatus.APPROVED;
        this.reviewedBy = requireReviewer(reviewedBy);
        this.reviewedAt = Instant.now();
        this.rejectionReason = null;
    }

    public void reject(String reviewedBy, String reason) {
        checkEligibilityForSubmission("rejected");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        this.status = KycStatus.REJECTED;
        this.reviewedBy = requireReviewer(reviewedBy);
        this.reviewedAt = Instant.now();
        this.rejectionReason = reason.strip();
    }

    private void checkEligibilityForSubmission(String verb) {
        if (status != KycStatus.SUBMITTED) {
            throw new InvalidStateTransitionException(
                    "Only a SUBMITTED verification can be " + verb + ", this one is " + status);
        }
    }

    private static String requireReviewer(String reviewedBy) {
        if (reviewedBy == null || reviewedBy.isBlank()) {
            throw new IllegalArgumentException("The reviewer is required on a KYC decision");
        }
        return reviewedBy.strip();
    }


}
