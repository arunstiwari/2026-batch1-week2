package com.fil.week2.dto;

import com.fil.week2.model.DocumentType;

public record KycSubmissionRequest(DocumentType documentType, String documentNumber) {
}
