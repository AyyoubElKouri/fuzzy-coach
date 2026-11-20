package com.ayyoub.fuzzycoach.fuzzyengine.core;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinguisticVariableTest {

    @Test
    void fluentBuilderKeepsTheDeclarationOrderOfTerms() {
        LinguisticVariable variable = LinguisticVariable
            .named("motivation")
            .range(0, 10)
            .term("low", new Triangle(0, 0, 5))
            .term("medium", new Triangle(2.5, 5, 7.5))
            .term("high", new Triangle(5, 10, 10))
            .build();

        assertEquals(List.of("low", "medium", "high"), List.copyOf(variable.terms().keySet()));
    }

    @Test
    void fluentBuilderRejectsADuplicateTerm() {
        LinguisticVariable.Builder builder = LinguisticVariable
            .named("motivation")
            .range(0, 10)
            .term("low", new Triangle(0, 0, 5));

        assertThrows(IllegalArgumentException.class,
            () -> builder.term("low", new Triangle(1, 2, 3)));
    }

    @Test
    void fluentBuilderRequiresARange() {
        LinguisticVariable.Builder builder = LinguisticVariable
            .named("motivation")
            .term("low", new Triangle(0, 0, 5));

        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
