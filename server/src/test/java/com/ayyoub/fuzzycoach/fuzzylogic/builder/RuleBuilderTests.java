package com.ayyoub.fuzzycoach.fuzzylogic.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.InvalidRuleException;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.Triangular;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Antecedent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import org.junit.jupiter.api.Test;

class RuleBuilderTests {

    @Test
    void buildsRuleWithOneConclusion() {
        LinguisticVariable motivation = variable("motivation");
        LinguisticVariable intensity = variable("intensity");

        Rule rule = Rule.builder()
                .when(Antecedent.of(motivation, motivation.term("high")))
                .then(intensity, intensity.term("high"))
                .build();

        assertEquals(1, rule.consequent().conclusions().size());
        assertEquals(
                "intensity",
                rule.consequent().conclusions().getFirst().variable().name());
    }

    @Test
    void addsSeveralConclusions() {
        LinguisticVariable motivation = variable("motivation");
        LinguisticVariable intensity = variable("intensity");

        Rule rule = Rule.builder()
                .when(Antecedent.of(motivation, motivation.term("high")))
                .then(intensity, intensity.term("high"))
                .andThen(intensity, intensity.term("low"))
                .build();

        assertEquals(2, rule.consequent().conclusions().size());
    }

    @Test
    void requiresAntecedent() {
        LinguisticVariable intensity = variable("intensity");
        RuleBuilder builder = Rule.builder().then(intensity, intensity.term("high"));

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("Antecedent"));
    }

    @Test
    void requiresConsequent() {
        LinguisticVariable motivation = variable("motivation");
        RuleBuilder builder =
                Rule.builder().when(Antecedent.of(motivation, motivation.term("high")));

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("Consequent"));
    }

    @Test
    void rejectsTermMissingFromOutputVariable() {
        LinguisticVariable motivation = variable("motivation");
        motivation.addTerm(new Term("medium", new Triangular(2.5, 5, 7.5)));
        LinguisticVariable intensity = variable("intensity");

        RuleBuilder builder =
                Rule.builder().when(Antecedent.of(motivation, motivation.term("high")));

        InvalidRuleException error = assertThrows(
                InvalidRuleException.class,
                () -> builder.then(intensity, motivation.term("medium")));
        assertTrue(error.getMessage().contains("Unknown term: medium"));
    }

    private static LinguisticVariable variable(String name) {
        return LinguisticVariable.builder()
                .named(name)
                .range(0, 10)
                .term("low", new Triangular(0, 0, 5))
                .term("high", new Triangular(5, 10, 10))
                .build();
    }
}
