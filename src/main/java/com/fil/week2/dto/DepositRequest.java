package com.fil.week2.dto;

import java.math.BigDecimal;

public record DepositRequest(String accountNumber, BigDecimal amount) {
}
