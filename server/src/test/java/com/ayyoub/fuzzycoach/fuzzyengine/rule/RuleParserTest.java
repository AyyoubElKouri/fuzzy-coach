package com.ayyoub.fuzzycoach.fuzzyengine.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

class RuleParserTest {

    @Test
    void parsesASingleConditionWithoutAnOperator() {
        Rule rule = new Rule("IF motivation is high THEN intensity is low");

        assertEquals(new SimpleCondition("motivation", "high"), rule.condition());
        assertEquals(new Conclusion("intensity", "low"), rule.conclusion());
    }

    @Test
    void parsesAndConditions() {
        Rule rule = new Rule("IF motivation is high AND stress is low THEN intensity is medium");

        assertEquals(
                new AndCondition(List.of(
                        new SimpleCondition("motivation", "high"),
                        new SimpleCondition("stress", "low"))),
                rule.condition());
    }

    @Test
    void parsesOrConditions() {
        Rule rule = new Rule("IF motivation is high OR stress is low THEN intensity is medium");

        assertEquals(
                new OrCondition(List.of(
                        new SimpleCondition("motivation", "high"),
                        new SimpleCondition("stress", "low"))),
                rule.condition());
    }

    @Test
    void andBindsTighterThanOr() {
        Rule rule = new Rule("IF motivation is high AND stress is low OR focus is high "
                + "THEN intensity is medium");

        assertEquals(
                new OrCondition(List.of(
                        new AndCondition(List.of(
                                new SimpleCondition("motivation", "high"),
                                new SimpleCondition("stress", "low"))),
                        new SimpleCondition("focus", "high"))),
                rule.condition());
    }

    @Test
    void parenthesesOverridePrecedence() {
        Rule rule = new Rule("IF motivation is high AND (stress is low OR focus is high) "
                + "THEN intensity is medium");

        assertEquals(
                new AndCondition(List.of(
                        new SimpleCondition("motivation", "high"),
                        new OrCondition(List.of(
                                new SimpleCondition("stress", "low"),
                                new SimpleCondition("focus", "high"))))),
                rule.condition());
    }

    @Test
    void flattensNestedSameOperatorConditions() {
        Rule rule = new Rule("IF (motivation is high AND stress is low) AND focus is high "
                + "THEN intensity is medium");

        assertEquals(
                new AndCondition(List.of(
                        new SimpleCondition("motivation", "high"),
                        new SimpleCondition("stress", "low"),
                        new SimpleCondition("focus", "high"))),
                rule.condition());
    }

    @Test
    void keywordsAreCaseInsensitive() {
        Rule rule = new Rule("if motivation IS high and stress is low then intensity is medium");

        assertEquals(
                new AndCondition(List.of(
                        new SimpleCondition("motivation", "high"),
                        new SimpleCondition("stress", "low"))),
                rule.condition());
    }

    @Test
    void rendersCanonicalTextThatParsesBackToTheSameRule() {
        Rule rule = new Rule("IF motivation is high AND (stress is low OR focus is high) "
                + "THEN intensity is medium");

        assertEquals(rule, new Rule(rule.toString()));
        assertEquals(
                "IF motivation is high AND (stress is low OR focus is high) "
                        + "THEN intensity is medium",
                rule.toString());
    }

    @Test
    void simpleConditionsAreCollectedLeftToRight() {
        Rule rule = new Rule("IF a is x OR b is y AND c is z THEN d is w");

        assertEquals(
                List.of(
                        new SimpleCondition("a", "x"),
                        new SimpleCondition("b", "y"),
                        new SimpleCondition("c", "z")),
                rule.condition().simpleConditions());
    }

    @Test
    void evaluatesTheTreeWithTheSuppliedLookup() {
        Rule rule = new Rule("IF motivation is high AND (stress is low OR focus is high) "
                + "THEN intensity is medium");

        double degree = rule.condition().degree(condition -> switch (condition.variableName()) {
            case "motivation" -> 0.8;
            case "stress" -> 0.2;
            case "focus" -> 0.5;
            default -> 0.0;
        });

        assertEquals(0.5, degree, 1e-9);
    }

    @Test
    void rejectsMissingThen() {
        RuleSyntaxException error = assertThrows(
                RuleSyntaxException.class,
                () -> new Rule("IF motivation is high intensity is low"));

        assertTrue(error.getMessage().contains("THEN"));
    }

    @Test
    void rejectsTrailingText() {
        assertThrows(
                RuleSyntaxException.class,
                () -> new Rule("IF motivation is high THEN intensity is low extra"));
    }

    @Test
    void rejectsUnbalancedParentheses() {
        assertThrows(
                RuleSyntaxException.class,
                () -> new Rule("IF (motivation is high THEN intensity is low"));
    }

    @Test
    void rejectsReservedWordsAsNames() {
        assertThrows(
                RuleSyntaxException.class, () -> new Rule("IF and is high THEN intensity is low"));
    }
}
