package com.fil.week2.model;

import com.fil.week2.exception.InvalidStateTransitionException;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * One submission of identity evidence and the decision made on it. A Customer
 * accumulates these; once decided, a verification is never edited again, so the
 * record of who refused someone and why survives their next attempt.
 */
@Entity
@Table(name = "kyc_verification")
public class KycVerification extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Convert(converter = DocumentNumberConverter.class)
    @Column(name = "document_number", nullable = false, length = 40)
    private DocumentNumber documentNumber;

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

    KycVerification(Customer customer, DocumentType documentType, DocumentNumber documentNumber) {
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

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public DocumentNumber getDocumentNumber() {
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

    /** A verification awaiting a decision. A Customer has at most one at a time. */
    public boolean isOpen() {
        return status == KycStatus.SUBMITTED;
    }

    public boolean isApproved() {
        return status == KycStatus.APPROVED;
    }

    void approve(String reviewedBy) {
        requireOpen("approved");
        this.reviewedBy = requireReviewer(reviewedBy);
        this.reviewedAt = Instant.now();
        this.status = KycStatus.APPROVED;
    }

    void reject(String reviewedBy, String reason) {
        requireOpen("rejected");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        this.reviewedBy = requireReviewer(reviewedBy);
        this.reviewedAt = Instant.now();
        this.rejectionReason = reason.strip();
        this.status = KycStatus.REJECTED;
    }

    private void requireOpen(String verb) {
        if (!isOpen()) {
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

    @Override
    public String toString() {
        return "KycVerification{" +
                "id=" + id +
                ", documentType=" + documentType +
                ", status=" + status +
                ", reviewedBy='" + reviewedBy + '\'' +
                ", reviewedAt=" + reviewedAt +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}
