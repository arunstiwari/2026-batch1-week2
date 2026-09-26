package com.fil.week2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawalRequest(@NotBlank String accountNumber,
                                @NotNull @Positive BigDecimal amount) {
}
