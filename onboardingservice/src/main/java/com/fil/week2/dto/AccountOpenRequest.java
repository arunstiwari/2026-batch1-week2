package com.fil.week2.dto;

import com.fil.week2.model.AccountType;
import jakarta.validation.constraints.NotNull;

/** An account always opens at a zero balance: money enters only through a Deposit. */
public record AccountOpenRequest(@NotNull AccountType type) {
}
