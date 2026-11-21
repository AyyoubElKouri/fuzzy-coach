package com.ayyoub.fuzzycoach.fuzzyengine.inference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

class RuleParserTest {

    @Test
    void parsesASingleConditionWithoutAnOperator() {
        Rule rule = new Rule("IF motivation is high THEN intensity is low");

        assertEquals(new Condition.Simple("motivation", "high"), rule.condition());
        assertEquals(new Conclusion("intensity", "low"), rule.conclusion());
    }

    @Test
    void parsesAndConditions() {
        Rule rule = new Rule("IF motivation is high AND stress is low THEN intensity is medium");

        assertEquals(
                new Condition.And(List.of(
                        new Condition.Simple("motivation", "high"),
                        new Condition.Simple("stress", "low"))),
                rule.condition());
    }

    @Test
    void parsesOrConditions() {
        Rule rule = new Rule("IF motivation is high OR stress is low THEN intensity is medium");

        assertEquals(
                new Condition.Or(List.of(
                        new Condition.Simple("motivation", "high"),
                        new Condition.Simple("stress", "low"))),
                rule.condition());
    }

    @Test
    void andBindsTighterThanOr() {
        Rule rule = new Rule("IF motivation is high AND stress is low OR focus is high "
                + "THEN intensity is medium");

        assertEquals(
                new Condition.Or(List.of(
                        new Condition.And(List.of(
                                new Condition.Simple("motivation", "high"),
                                new Condition.Simple("stress", "low"))),
                        new Condition.Simple("focus", "high"))),
                rule.condition());
    }

    @Test
    void parenthesesOverridePrecedence() {
        Rule rule = new Rule("IF motivation is high AND (stress is low OR focus is high) "
                + "THEN intensity is medium");

        assertEquals(
                new Condition.And(List.of(
                        new Condition.Simple("motivation", "high"),
                        new Condition.Or(List.of(
                                new Condition.Simple("stress", "low"),
                                new Condition.Simple("focus", "high"))))),
                rule.condition());
    }

    @Test
    void flattensNestedSameOperatorConditions() {
        Rule rule = new Rule("IF (motivation is high AND stress is low) AND focus is high "
                + "THEN intensity is medium");

        assertEquals(
                new Condition.And(List.of(
                        new Condition.Simple("motivation", "high"),
                        new Condition.Simple("stress", "low"),
                        new Condition.Simple("focus", "high"))),
                rule.condition());
    }

    @Test
    void keywordsAreCaseInsensitive() {
        Rule rule = new Rule("if motivation IS high and stress is low then intensity is medium");

        assertEquals(
                new Condition.And(List.of(
                        new Condition.Simple("motivation", "high"),
                        new Condition.Simple("stress", "low"))),
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
                        new Condition.Simple("a", "x"),
                        new Condition.Simple("b", "y"),
                        new Condition.Simple("c", "z")),
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
    void parsesNotConditions() {
        Rule rule = new Rule("IF NOT motivation is high THEN intensity is low");

        assertEquals(
                new Condition.Not(new Condition.Simple("motivation", "high")), rule.condition());
    }

    @Test
    void notBindsTighterThanAnd() {
        Rule rule =
                new Rule("IF NOT motivation is high AND stress is low THEN intensity is medium");

        assertEquals(
                new Condition.And(List.of(
                        new Condition.Not(new Condition.Simple("motivation", "high")),
                        new Condition.Simple("stress", "low"))),
                rule.condition());
    }

    @Test
    void notAppliesToAParenthesizedGroup() {
        Rule rule = new Rule("IF NOT (motivation is high AND stress is low) THEN intensity is low");

        assertEquals(
                new Condition.Not(new Condition.And(List.of(
                        new Condition.Simple("motivation", "high"),
                        new Condition.Simple("stress", "low")))),
                rule.condition());
        assertEquals(rule, new Rule(rule.toString()));
    }

    @Test
    void evaluatesNotAsTheComplement() {
        Rule rule = new Rule("IF NOT motivation is high THEN intensity is low");

        double degree = rule.condition().degree(condition -> 0.25);

        assertEquals(0.75, degree, 1e-9);
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
