package com.ayyoub.fuzzycoach.fuzzyengine;

import java.util.List;
import java.util.Map;

import com.ayyoub.fuzzycoach.fuzzyengine.core.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Triangle;
import com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier.CentroidDefuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier.Defuzzifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuzzyEngineTest {

    private static final LinguisticVariable MOTIVATION = new LinguisticVariable(
        "motivation", 0, 10,
        Map.of(
            "low", new Triangle(0, 0, 6),
            "high", new Triangle(4, 10, 10)));

    private static final LinguisticVariable INTENSITY = new LinguisticVariable(
        "intensity", 0, 10,
        Map.of(
            "low", new Triangle(0, 0, 6),
            "high", new Triangle(4, 10, 10)));

    private static final List<Rule> RULES = List.of(
        new Rule("IF motivation is low THEN intensity is low"),
        new Rule("IF motivation is high THEN intensity is high"));

    private static FuzzyEngine engine(Defuzzifier defuzzifier) {
        return new FuzzyEngine(List.of(MOTIVATION), List.of(INTENSITY), RULES, defuzzifier);
    }

    private static FuzzyEngine engine() {
        return new FuzzyEngine(List.of(MOTIVATION), List.of(INTENSITY), RULES);
    }

    @Test
    void symmetricInputGivesMiddleOutput() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 5.0));
        assertEquals(5.0, result.output("intensity"), 0.05);
    }

    @Test
    void lowInputGivesLowOutput() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 2.0));
        assertTrue(result.output("intensity") < 4.0,
            "expected a low intensity, got " + result.output("intensity"));
    }

    @Test
    void highInputGivesHighOutput() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 8.0));
        assertTrue(result.output("intensity") > 6.0,
            "expected a high intensity, got " + result.output("intensity"));
    }

    @Test
    void exposesTheFuzzyValueOfTheOutput() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 5.0));
        Map<String, Double> degrees = result.fuzzyValues().get("intensity").degrees();

        assertEquals(2, degrees.size());
        assertTrue(degrees.get("low") > 0.0);
        assertTrue(degrees.get("high") > 0.0);
    }

    @Test
    void centroidStaysInTheUniverseAndHitsTheMiddleOfASymmetricInput() {
        Defuzzifier defuzzifier = new CentroidDefuzzifier();
        double value = engine(defuzzifier).evaluate(Map.of("motivation", 5.0)).output("intensity");

        assertTrue(value >= 0.0 && value <= 10.0, "left the universe: " + value);
        assertEquals(5.0, value, 0.1, "disagreed on a symmetric input");
    }

    @Test
    void rejectsUnknownConditionVariable() {
        Rule badRule = new Rule("IF stress is low THEN intensity is low");

        assertThrows(IllegalArgumentException.class,
            () -> new FuzzyEngine(List.of(MOTIVATION), List.of(INTENSITY), List.of(badRule)));
    }

    @Test
    void rejectsUnknownConclusionTerm() {
        Rule badRule = new Rule("IF motivation is low THEN intensity is extreme");

        assertThrows(IllegalArgumentException.class,
            () -> new FuzzyEngine(List.of(MOTIVATION), List.of(INTENSITY), List.of(badRule)));
    }

    @Test
    void rejectsVariableDeclaredAsBothInputAndOutput() {
        assertThrows(IllegalArgumentException.class,
            () -> new FuzzyEngine(List.of(MOTIVATION), List.of(MOTIVATION), RULES));
    }

    @Test
    void rejectsMissingInputValue() {
        FuzzyEngine engine = engine();
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(Map.of()));
    }

    @Test
    void rejectsUnknownInputValue() {
        FuzzyEngine engine = engine();
        assertThrows(IllegalArgumentException.class,
            () -> engine.evaluate(Map.of("motivation", 5.0, "stress", 2.0)));
    }

    @Test
    void noActiveRuleFallsBackToTheMiddleOfTheUniverse() {
        LinguisticVariable narrow = new LinguisticVariable(
            "motivation", 0, 10,
            Map.of("low", new Triangle(0, 0, 1)));

        Rule rule = new Rule("IF motivation is low THEN intensity is low");

        FuzzyEngine engine = new FuzzyEngine(List.of(narrow), List.of(INTENSITY), List.of(rule));

        FuzzyResult result = engine.evaluate(Map.of("motivation", 9.0));
        assertEquals(5.0, result.output("intensity"), 0.05);
    }
}
