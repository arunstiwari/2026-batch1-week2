package com.fil.week2.model;

import jakarta.persistence.Embeddable;

/**
 * Immutable and compared by value: two addresses with the same contents are the
 * same address. Replacing one means constructing a new one.
 */
@Embeddable
public record Address(String street, String city, String zip) {
}
