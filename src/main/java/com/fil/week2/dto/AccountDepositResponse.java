package com.fil.week2.dto;

import java.math.BigDecimal;

public record AccountDepositResponse(String message, BigDecimal balance) {
}
