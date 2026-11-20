package com.ayyoub.fuzzycoach.fuzzyengine;

import java.util.List;
import java.util.Map;

import com.ayyoub.fuzzycoach.fuzzyengine.core.FuzzyValue;
import com.ayyoub.fuzzycoach.fuzzyengine.core.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Triangle;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.Inference;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.InferenceImpl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InferenceTest {

    private final LinguisticVariable motivation = new LinguisticVariable(
        "motivation", 0, 10,
        Map.of(
            "low", new Triangle(0, 0, 5),
            "high", new Triangle(5, 10, 10)));

    private final LinguisticVariable intensity = new LinguisticVariable(
        "intensity", 0, 10,
        Map.of(
            "low", new Triangle(0, 0, 5),
            "high", new Triangle(5, 10, 10)));

    private final List<LinguisticVariable> outputs = List.of(intensity);

    @Test
    void andTakesTheMinimum() {
        Rule rule = new Rule(
            "IF motivation is low AND motivation is high THEN intensity is low");

        Map<String, Map<String, Double>> fuzzified = Map.of(
            "motivation", Map.of("low", 0.8, "high", 0.3));

        Map<String, FuzzyValue> result =
            new InferenceImpl().evaluate(List.of(rule), fuzzified, outputs);

        assertEquals(0.3, result.get("intensity").degrees().get("low"), 1e-9);
    }

    @Test
    void orTakesTheMaximum() {
        Rule rule = new Rule(
            "IF motivation is low OR motivation is high THEN intensity is low");

        Map<String, Map<String, Double>> fuzzified = Map.of(
            "motivation", Map.of("low", 0.8, "high", 0.3));

        Map<String, FuzzyValue> result =
            new InferenceImpl().evaluate(List.of(rule), fuzzified, outputs);

        assertEquals(0.8, result.get("intensity").degrees().get("low"), 1e-9);
    }

    @Test
    void mixedOperatorsRespectPrecedence() {
        Rule rule = new Rule(
            "IF motivation is low AND stress is low OR motivation is high "
                + "THEN intensity is low");

        Map<String, Map<String, Double>> fuzzified = Map.of(
            "motivation", Map.of("low", 0.8, "high", 0.9),
            "stress", Map.of("low", 0.2));

        Map<String, FuzzyValue> result =
            new InferenceImpl().evaluate(List.of(rule), fuzzified, outputs);

        assertEquals(0.9, result.get("intensity").degrees().get("low"), 1e-9);
    }

    @Test
    void rulesConcludingTheSameTermMergeByMaximum() {
        Rule rule1 = new Rule("IF motivation is low THEN intensity is low");
        Rule rule2 = new Rule("IF motivation is high THEN intensity is low");

        Map<String, Map<String, Double>> fuzzified = Map.of(
            "motivation", Map.of("low", 0.2, "high", 0.7));

        Map<String, FuzzyValue> result =
            new InferenceImpl().evaluate(List.of(rule1, rule2), fuzzified, outputs);

        assertEquals(0.7, result.get("intensity").degrees().get("low"), 1e-9);
    }

    @Test
    void unknownVariableGivesAClearError() {
        Rule rule = new Rule("IF stress is low THEN intensity is low");

        Map<String, Map<String, Double>> fuzzified = Map.of(
            "motivation", Map.of("low", 0.8, "high", 0.3));

        Inference inference = new InferenceImpl();
        assertThrows(IllegalArgumentException.class,
            () -> inference.evaluate(List.of(rule), fuzzified, outputs));
    }

    @Test
    void unknownTermGivesAClearError() {
        Rule rule = new Rule("IF motivation is medium THEN intensity is low");

        Map<String, Map<String, Double>> fuzzified = Map.of(
            "motivation", Map.of("low", 0.8, "high", 0.3));

        Inference inference = new InferenceImpl();
        assertThrows(IllegalArgumentException.class,
            () -> inference.evaluate(List.of(rule), fuzzified, outputs));
    }

    @Test
    void unknownOutputVariableGivesAClearError() {
        Rule rule = new Rule("IF motivation is low THEN duration is low");

        Map<String, Map<String, Double>> fuzzified = Map.of(
            "motivation", Map.of("low", 0.8, "high", 0.3));

        Inference inference = new InferenceImpl();
        assertThrows(IllegalArgumentException.class,
            () -> inference.evaluate(List.of(rule), fuzzified, outputs));
    }
}
