package com.ayyoub.fuzzycoach.fuzzylogic.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ayyoub.fuzzycoach.fuzzylogic.FuzzyLogicEngine;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;
import com.ayyoub.fuzzycoach.fuzzylogic.inference.Mamdani;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.Triangular;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Antecedent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Consequent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import org.junit.jupiter.api.Test;

class FuzzyEngineBuilderTests {

    @Test
    void buildsAndEvaluatesEngine() {
        LinguisticVariable motivation = variable("motivation");
        Term high = motivation.term("high");
        LinguisticVariable intensity = variable("intensity");
        Term outputHigh = intensity.term("high");

        FuzzyLogicEngine engine = FuzzyEngineBuilder.builder()
                .input(motivation)
                .output(intensity)
                .rule(new Rule(
                        Antecedent.of(motivation, high), new Consequent(intensity, outputHigh)))
                .inference(new Mamdani())
                .build();

        engine.setVariable("motivation", 9.5);
        engine.evaluate();

        assertEquals(8.333333333333334, engine.output("intensity"), 0.05);
    }

    @Test
    void acceptsSeveralRules() {
        LinguisticVariable motivation = variable("motivation");
        LinguisticVariable intensity = variable("intensity");
        Rule low = new Rule(
                Antecedent.of(motivation, motivation.term("low")),
                new Consequent(intensity, intensity.term("low")));
        Rule high = new Rule(
                Antecedent.of(motivation, motivation.term("high")),
                new Consequent(intensity, intensity.term("high")));

        FuzzyLogicEngine engine = FuzzyEngineBuilder.builder()
                .input(motivation)
                .output(intensity)
                .rules(low, high)
                .inference(new Mamdani())
                .build();

        engine.setVariable("motivation", 9.5);
        engine.evaluate();

        assertTrue(engine.output("intensity") > 5.0);
    }

    @Test
    void requiresInputVariable() {
        LinguisticVariable intensity = variable("intensity");
        FuzzyEngineBuilder builder = FuzzyEngineBuilder.builder().output(intensity);

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("input"));
    }

    @Test
    void requiresOutputVariable() {
        LinguisticVariable motivation = variable("motivation");
        FuzzyEngineBuilder builder = FuzzyEngineBuilder.builder().input(motivation);

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("output"));
    }

    @Test
    void requiresRule() {
        LinguisticVariable motivation = variable("motivation");
        LinguisticVariable intensity = variable("intensity");

        FuzzyEngineBuilder builder = FuzzyEngineBuilder.builder()
                .input(motivation)
                .output(intensity)
                .inference(new Mamdani());

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("rule"));
    }

    @Test
    void requiresInference() {
        LinguisticVariable motivation = variable("motivation");
        LinguisticVariable intensity = variable("intensity");
        Rule rule = new Rule(
                Antecedent.of(motivation, motivation.term("high")),
                new Consequent(intensity, intensity.term("high")));

        FuzzyEngineBuilder builder =
                FuzzyEngineBuilder.builder().input(motivation).output(intensity).rule(rule);

        ConfigurationException error = assertThrows(ConfigurationException.class, builder::build);
        assertTrue(error.getMessage().contains("Inference"));
    }

    @Test
    void rejectsDuplicateVariableNames() {
        LinguisticVariable first = variable("intensity");
        LinguisticVariable second = variable("intensity");
        FuzzyEngineBuilder builder = FuzzyEngineBuilder.builder().input(first);

        ConfigurationException error =
                assertThrows(ConfigurationException.class, () -> builder.output(second));
        assertTrue(error.getMessage().contains("Duplicate variable: intensity"));
    }

    @Test
    void rejectsVariableWithoutTerms() {
        LinguisticVariable empty = new LinguisticVariable("empty", 0, 10);
        FuzzyEngineBuilder builder = FuzzyEngineBuilder.builder();

        ConfigurationException error =
                assertThrows(ConfigurationException.class, () -> builder.input(empty));
        assertTrue(error.getMessage().contains("no terms"));
    }

    private static LinguisticVariable variable(String name) {
        LinguisticVariable variable = new LinguisticVariable(name, 0, 10);
        variable.addTerm(new Term("low", new Triangular(0, 0, 5)));
        variable.addTerm(new Term("high", new Triangular(5, 10, 10)));

        return variable;
    }
}
