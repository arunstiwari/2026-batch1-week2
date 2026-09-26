package com.fil.week2.dto;

import jakarta.validation.constraints.NotBlank;

public record KycApprovalRequest(@NotBlank String reviewedBy) {
}
