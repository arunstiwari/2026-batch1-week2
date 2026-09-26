package com.fil.week2.dto;

import com.fil.week2.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KycSubmissionRequest(@NotNull DocumentType documentType,
                                   @NotBlank String documentNumber) {
}
