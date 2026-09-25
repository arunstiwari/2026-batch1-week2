package com.fil.week2.dto;

import com.fil.week2.model.DocumentType;
import com.fil.week2.model.KycStatus;
import com.fil.week2.model.KycVerification;

import java.time.Instant;

public record KycResponse(
        Long customerId,
        DocumentType documentType,
        String documentNumber,
        KycStatus status,
        String reviewedBy,
        Instant reviewedAt,
        String rejectionReason,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static KycResponse from(KycVerification kyc) {
        return new KycResponse(
                kyc.getCustomer() == null ? kyc.getId() : kyc.getCustomer().getId(),
                kyc.getDocumentType(),
                mask(kyc.getDocumentNumber()),
                kyc.getStatus(),
                kyc.getReviewedBy(),
                kyc.getReviewedAt(),
                kyc.getRejectionReason(),
                kyc.getVersion(),
                kyc.getCreatedAt(),
                kyc.getUpdatedAt());
    }

    /** The document number is stored in full but never echoed in full: last four digits only. */
    private static String mask(String documentNumber) {
        if (documentNumber == null || documentNumber.length() <= 4) {
            return "****";
        }
        return "*".repeat(documentNumber.length() - 4)
                + documentNumber.substring(documentNumber.length() - 4);
    }
}
