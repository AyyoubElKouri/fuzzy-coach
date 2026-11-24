package com.ayyoub.fuzzycoach.fuzzylogic.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.Triangular;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

import org.junit.jupiter.api.Test;

class LinguisticVariableBuilderTests {

    @Test
    void buildsVariable() {
        LinguisticVariable variable = LinguisticVariable.builder()
                .named("motivation")
                .range(0, 10)
                .term("low", new Triangular(0, 0, 5))
                .term("high", new Triangular(5, 10, 10))
                .build();

        assertEquals("motivation", variable.name());
        assertEquals(0.0, variable.minimum());
        assertEquals(10.0, variable.maximum());
        assertEquals(2, variable.terms().size());
        assertTrue(variable.terms().containsKey("high"));
    }

    @Test
    void requiresName() {
        LinguisticVariableBuilder builder =
                LinguisticVariable.builder().range(0, 10).term("low", new Triangular(0, 0, 5));

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("Variable name"));
    }

    @Test
    void requiresRange() {
        LinguisticVariableBuilder builder = LinguisticVariable.builder()
                .named("motivation")
                .term("low", new Triangular(0, 0, 5));

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("Range"));
    }

    @Test
    void requiresTerm() {
        LinguisticVariableBuilder builder =
                LinguisticVariable.builder().named("motivation").range(0, 10);

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("term"));
    }

    @Test
    void rejectsDuplicateTerm() {
        LinguisticVariableBuilder builder = LinguisticVariable.builder()
                .named("motivation")
                .range(0, 10)
                .term("low", new Triangular(0, 0, 5));

        ConfigurationException error = assertThrows(
                ConfigurationException.class, () -> builder.term("low", new Triangular(0, 0, 5)));
        assertTrue(error.getMessage().contains("Duplicate term: low"));
    }

    @Test
    void wrapsInvalidRange() {
        LinguisticVariableBuilder builder = LinguisticVariable.builder()
                .named("motivation")
                .range(10, 0)
                .term("low", new Triangular(0, 0, 5));

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("Minimum must be less than maximum"));
    }
}
