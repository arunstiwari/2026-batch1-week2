package com.fil.week2.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountNumberConverter implements AttributeConverter<AccountNumber, String> {

    @Override
    public String convertToDatabaseColumn(AccountNumber attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public AccountNumber convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AccountNumber.of(dbData);
    }
}
