package com.fil.week2.model;

import java.util.Objects;

/**
 * The identifying number on an Identity Document. Held in full, but it knows how
 * to show itself: only the last four characters are ever disclosed.
 */
public final class DocumentNumber {

    private static final int DISCLOSED = 4;

    private final String value;

    private DocumentNumber(String value) {
        this.value = value;
    }

    public static DocumentNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A document number is required");
        }
        return new DocumentNumber(value.strip());
    }

    public String value() {
        return value;
    }

    /** The masking rule had two identical homes in the response DTOs. This is its one. */
    public String masked() {
        if (value.length() <= DISCLOSED) {
            return "*".repeat(DISCLOSED);
        }
        return "*".repeat(value.length() - DISCLOSED) + value.substring(value.length() - DISCLOSED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return value.equals(((DocumentNumber) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /** Never the full value: this ends up in logs. */
    @Override
    public String toString() {
        return masked();
    }
}
