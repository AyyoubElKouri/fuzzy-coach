package com.ayyoub.fuzzycoach.fuzzyengine.rule;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses rule text such as
 * {@code "IF motivation is high AND stress is low THEN intensity is medium"}.
 *
 * <p>{@code AND} binds tighter than {@code OR}. Parentheses override the
 * precedence. The grammar is:
 *
 * <pre>
 * rule        := IF orExpression THEN condition EOF
 * orExpression := andExpression (OR andExpression)*
 * andExpression := atom (AND atom)*
 * atom        := condition | '(' orExpression ')'
 * condition   := IDENTIFIER IS IDENTIFIER
 * </pre>
 *
 * <p>Keywords are case-insensitive. Names are case-sensitive and match
 * {@code [A-Za-z_][A-Za-z0-9_]*}. The words {@code if}, {@code then},
 * {@code and}, {@code or} and {@code is} are reserved.
 */
final class RuleParser {

    private final String text;
    private int position;

    private RuleParser(String text) {
        if (text == null || text.isBlank()) {
            throw new RuleSyntaxException("a rule needs text", 0, "");
        }

        this.text = text;
    }

    /**
     * @throws RuleSyntaxException when {@code text} is not a valid rule
     */
    static ParsedRule parse(String text) {
        return new RuleParser(text).parseRule();
    }

    private ParsedRule parseRule() {
        expectKeyword("if");
        Condition condition = parseOrExpression();
        expectKeyword("then");
        Conclusion conclusion = parseConclusion();
        skipWhitespace();

        if (position != text.length()) {
            throw error("unexpected text after the conclusion");
        }

        return new ParsedRule(condition, conclusion);
    }

    private Condition parseOrExpression() {
        List<Condition> conditions = new ArrayList<>();
        addOrCondition(conditions, parseAndExpression());

        while (matchKeyword("or")) {
            addOrCondition(conditions, parseAndExpression());
        }

        return conditions.size() == 1 ? conditions.get(0) : new OrCondition(conditions);
    }

    private Condition parseAndExpression() {
        List<Condition> conditions = new ArrayList<>();

        do {
            addAndCondition(conditions, parseAtom());
        } while (matchKeyword("and"));

        return conditions.size() == 1 ? conditions.get(0) : new AndCondition(conditions);
    }

    private Condition parseAtom() {
        skipWhitespace();

        if (match('(')) {
            Condition condition = parseOrExpression();
            expect(')');
            return condition;
        }

        return parseSimpleCondition();
    }

    private SimpleCondition parseSimpleCondition() {
        String variableName = readIdentifier();
        expectKeyword("is");
        String termName = readIdentifier();

        return new SimpleCondition(variableName, termName);
    }

    private Conclusion parseConclusion() {
        String variableName = readIdentifier();
        expectKeyword("is");
        String termName = readIdentifier();

        return new Conclusion(variableName, termName);
    }

    private static void addOrCondition(List<Condition> conditions, Condition condition) {
        if (condition instanceof OrCondition or) {
            conditions.addAll(or.conditions());
            return;
        }

        conditions.add(condition);
    }

    private static void addAndCondition(List<Condition> conditions, Condition condition) {
        if (condition instanceof AndCondition and) {
            conditions.addAll(and.conditions());
            return;
        }

        conditions.add(condition);
    }

    private void skipWhitespace() {
        while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
            position++;
        }
    }

    private boolean match(char expected) {
        skipWhitespace();

        if (position >= text.length() || text.charAt(position) != expected) {
            return false;
        }

        position++;

        return true;
    }

    private void expect(char expected) {
        if (!match(expected)) {
            throw error("expected '" + expected + "'");
        }
    }

    private boolean matchKeyword(String keyword) {
        skipWhitespace();

        int end = position + keyword.length();
        if (end > text.length()) {
            return false;
        }

        if (!text.regionMatches(true, position, keyword, 0, keyword.length())) {
            return false;
        }

        if (end < text.length() && isIdentifierPart(text.charAt(end))) {
            return false;
        }

        position = end;

        return true;
    }

    private void expectKeyword(String keyword) {
        if (!matchKeyword(keyword)) {
            throw error("expected '" + keyword.toUpperCase() + "'");
        }
    }

    private String readIdentifier() {
        skipWhitespace();

        if (position >= text.length() || !isIdentifierStart(text.charAt(position))) {
            throw error("expected an identifier");
        }

        int start = position;
        position++;

        while (position < text.length() && isIdentifierPart(text.charAt(position))) {
            position++;
        }

        String identifier = text.substring(start, position);
        if (isReserved(identifier)) {
            throw new RuleSyntaxException(
                    "reserved word '" + identifier + "' cannot be used as a name", start, text);
        }

        return identifier;
    }

    private static boolean isReserved(String identifier) {
        return identifier.equalsIgnoreCase("if")
                || identifier.equalsIgnoreCase("then")
                || identifier.equalsIgnoreCase("and")
                || identifier.equalsIgnoreCase("or")
                || identifier.equalsIgnoreCase("is");
    }

    private static boolean isIdentifierStart(char character) {
        return (character >= 'A' && character <= 'Z')
                || (character >= 'a' && character <= 'z')
                || character == '_';
    }

    private static boolean isIdentifierPart(char character) {
        return isIdentifierStart(character) || (character >= '0' && character <= '9');
    }

    private RuleSyntaxException error(String detail) {
        return new RuleSyntaxException(detail, position, text);
    }

    /**
     * The two components of a parsed rule.
     */
    record ParsedRule(Condition condition, Conclusion conclusion) {}
}
