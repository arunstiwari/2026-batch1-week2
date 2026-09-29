package com.fil.week2.dto;

import com.fil.week2.model.Money;

public record AccountDepositResponse(String message, Money balance) {
}
