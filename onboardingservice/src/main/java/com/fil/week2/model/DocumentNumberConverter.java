package com.fil.week2.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DocumentNumberConverter implements AttributeConverter<DocumentNumber, String> {

    @Override
    public String convertToDatabaseColumn(DocumentNumber attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public DocumentNumber convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DocumentNumber.of(dbData);
    }
}
