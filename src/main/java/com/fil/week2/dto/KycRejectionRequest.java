package com.fil.week2.dto;

import jakarta.validation.constraints.NotBlank;

public record KycRejectionRequest(@NotBlank String reviewedBy,
                                  @NotBlank String reason) {
}
