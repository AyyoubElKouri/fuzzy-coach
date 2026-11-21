package com.ayyoub.fuzzycoach.fuzzyengine.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier.CentroidDefuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier.Defuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.RuleActivation;
import com.ayyoub.fuzzycoach.fuzzyengine.membership.Triangle;
import com.ayyoub.fuzzycoach.fuzzyengine.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.rule.RuleSyntaxException;
import com.ayyoub.fuzzycoach.fuzzyengine.variable.LinguisticVariable;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class FuzzyEngineTest {

    private static final LinguisticVariable MOTIVATION = new LinguisticVariable(
            "motivation",
            0,
            10,
            Map.of(
                    "low", new Triangle(0, 0, 6),
                    "high", new Triangle(4, 10, 10)));

    private static final LinguisticVariable INTENSITY = new LinguisticVariable(
            "intensity",
            0,
            10,
            Map.of(
                    "low", new Triangle(0, 0, 6),
                    "high", new Triangle(4, 10, 10)));

    private static final List<Rule> RULES = List.of(
            new Rule("IF motivation is low THEN intensity is low"),
            new Rule("IF motivation is high THEN intensity is high"));

    private static FuzzyEngine engine(Defuzzifier defuzzifier) {
        return FuzzyEngine.builder()
                .input(MOTIVATION)
                .output(INTENSITY)
                .rules(RULES)
                .defuzzifier(defuzzifier)
                .build();
    }

    private static FuzzyEngine engine() {
        return FuzzyEngine.builder()
                .input(MOTIVATION)
                .output(INTENSITY)
                .rules(RULES)
                .build();
    }

    @Test
    void symmetricInputGivesMiddleOutput() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 5.0));
        assertEquals(5.0, result.output("intensity"), 0.05);
    }

    @Test
    void lowInputGivesLowOutput() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 2.0));
        assertTrue(
                result.output("intensity") < 4.0,
                "expected a low intensity, got " + result.output("intensity"));
    }

    @Test
    void highInputGivesHighOutput() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 8.0));
        assertTrue(
                result.output("intensity") > 6.0,
                "expected a high intensity, got " + result.output("intensity"));
    }

    @Test
    void acceptsTypedInputs() {
        FuzzyResult result = engine().evaluate(Inputs.of("motivation", 5.0));

        assertEquals(5.0, result.output("intensity"), 0.05);
        assertEquals(5.0, result.findOutput("intensity").orElseThrow(), 1e-9);
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
    void explainsWhichRulesFiredStrongestFirst() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 5.0));

        List<RuleActivation> explanation = result.explanation("intensity");

        assertEquals(2, explanation.size());
        assertTrue(explanation.get(0).activation() >= explanation.get(1).activation());
        assertEquals("intensity", explanation.get(0).rule().conclusion().variableName());
        assertTrue(explanation.get(0).activation() > 0.0);
    }

    @Test
    void explanationRejectsAnUnknownOutput() {
        FuzzyResult result = engine().evaluate(Map.of("motivation", 5.0));

        assertThrows(IllegalArgumentException.class, () -> result.explanation("stress"));
    }

    @Test
    void centroidStaysInTheUniverseAndHitsTheMiddleOfASymmetricInput() {
        Defuzzifier defuzzifier = new CentroidDefuzzifier();
        double value = engine(defuzzifier).evaluate(Map.of("motivation", 5.0)).output("intensity");

        assertTrue(value >= 0.0 && value <= 10.0, "left the universe: " + value);
        assertEquals(5.0, value, 0.1, "disagreed on a symmetric input");
    }

    @Test
    void builderParsesRulesFromText() {
        FuzzyEngine engine = FuzzyEngine.builder()
                .input(MOTIVATION)
                .output(INTENSITY)
                .rules(
                        "IF motivation is low THEN intensity is low",
                        "IF motivation is high THEN intensity is high")
                .build();

        assertEquals(5.0, engine.evaluate(Map.of("motivation", 5.0)).output("intensity"), 0.05);
    }

    @Test
    void builderReportsWhichRuleTextFailedToParse() {
        RuleSyntaxException error = assertThrows(
                RuleSyntaxException.class,
                () -> FuzzyEngine.builder()
                        .input(MOTIVATION)
                        .output(INTENSITY)
                        .rules(
                                "IF motivation is low THEN intensity is low",
                                "IF motivation is high intensity is high"));

        assertTrue(error.getMessage().contains("rule 2 of 2"));
    }

    @Test
    void rejectsUnknownConditionVariable() {
        Rule badRule = new Rule("IF stress is low THEN intensity is low");

        assertThrows(
                IllegalArgumentException.class,
                () -> FuzzyEngine.builder()
                        .input(MOTIVATION)
                        .output(INTENSITY)
                        .rule(badRule)
                        .build());
    }

    @Test
    void rejectsUnknownConclusionTerm() {
        Rule badRule = new Rule("IF motivation is low THEN intensity is extreme");

        assertThrows(
                IllegalArgumentException.class,
                () -> FuzzyEngine.builder()
                        .input(MOTIVATION)
                        .output(INTENSITY)
                        .rule(badRule)
                        .build());
    }

    @Test
    void rejectsVariableDeclaredAsBothInputAndOutput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FuzzyEngine.builder()
                        .input(MOTIVATION)
                        .output(MOTIVATION)
                        .rules(RULES)
                        .build());
    }

    @Test
    void rejectsMissingInputValue() {
        FuzzyEngine engine = engine();
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(Inputs.from(Map.of())));
    }

    @Test
    void rejectsUnknownInputValue() {
        FuzzyEngine engine = engine();
        assertThrows(
                IllegalArgumentException.class, () -> engine.evaluate(Inputs.of("stress", 2.0)));
    }

    @Test
    void noActiveRuleFallsBackToTheMiddleOfTheUniverse() {
        LinguisticVariable narrow =
                new LinguisticVariable("motivation", 0, 10, Map.of("low", new Triangle(0, 0, 1)));

        FuzzyEngine engine = FuzzyEngine.builder()
                .input(narrow)
                .output(INTENSITY)
                .rules("IF motivation is low THEN intensity is low")
                .build();

        FuzzyResult result = engine.evaluate(Map.of("motivation", 9.0));
        assertEquals(5.0, result.output("intensity"), 0.05);
    }
}
