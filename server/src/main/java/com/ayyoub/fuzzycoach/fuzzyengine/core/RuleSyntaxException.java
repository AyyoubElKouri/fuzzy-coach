package com.ayyoub.fuzzycoach.fuzzyengine.core;

/**
 * A rule text that does not follow the rule grammar.
 *
 * <p>The offset is zero-based and points at the first unexpected character.
 */
public final class RuleSyntaxException extends IllegalArgumentException {

    private final String detail;
    private final int offset;
    private final String ruleText;

    public RuleSyntaxException(String detail, int offset, String ruleText) {
        super(buildMessage(detail, offset, ruleText));

        this.detail = detail;
        this.offset = offset;
        this.ruleText = ruleText;
    }

    /**
     * @return the short description of the error, without position and text
     */
    public String detail() {
        return detail;
    }

    /**
     * @return the zero-based offset of the error
     */
    public int offset() {
        return offset;
    }

    /**
     * @return the rule text that failed to parse
     */
    public String ruleText() {
        return ruleText;
    }

    private static String buildMessage(String detail, int offset, String ruleText) {
        return "Rule syntax error at offset " + offset + ": " + detail
            + System.lineSeparator()
            + ruleText
            + System.lineSeparator()
            + " ".repeat(offset) + "^";
    }
}
