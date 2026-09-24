package com.fil.week2.dto;

import com.fil.week2.model.AccountType;

import java.math.BigDecimal;

public record AccountOpenRequest(AccountType type, BigDecimal balance) {
}
