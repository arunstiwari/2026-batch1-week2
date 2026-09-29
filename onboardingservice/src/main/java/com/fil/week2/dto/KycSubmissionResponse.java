package com.fil.week2.dto;

import com.fil.week2.model.DocumentType;
import com.fil.week2.model.KycStatus;
import com.fil.week2.model.KycVerification;

import java.time.Instant;

public record KycSubmissionResponse(Long customerId,
                                    DocumentType documentType,
                                    String documentNumber,
                                    KycStatus status,
                                    long version,
                                    Instant createdAt,
                                    Instant updatedAt
) {
    public static KycSubmissionResponse from(KycVerification kyc) {
        return new KycSubmissionResponse(
                kyc.getCustomer().getId(),
                kyc.getDocumentType(),
                kyc.getDocumentNumber().masked(),
                kyc.getStatus(),
                kyc.getVersion(),
                kyc.getCreatedAt(),
                kyc.getUpdatedAt());
    }

}

