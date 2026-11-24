package com.ayyoub.fuzzycoach.fuzzylogic.io;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Raw shape of an engine configuration file.
 *
 * <p>The records in this file mirror the JSON document one to one. They carry no behaviour: the
 * {@link EngineAssembler} turns them into a working {@link
 * com.ayyoub.fuzzycoach.fuzzylogic.FuzzyLogicEngine}.
 */
record EngineDefinition(
        InferenceDefinition inference,
        List<VariableDefinition> inputs,
        List<VariableDefinition> outputs,
        List<RuleDefinition> rules) {

    record InferenceDefinition(
            String type,
            String and,
            String or,
            String not,
            String defuzzifier,
            Integer resolution) {}

    record VariableDefinition(
            String name, Double minimum, Double maximum, List<TermDefinition> terms) {}

    record TermDefinition(String name, MembershipDefinition membership) {}

    record MembershipDefinition(String type, List<Double> parameters) {}

    record RuleDefinition(
            @JsonProperty("if") AntecedentDefinition condition,
            @JsonProperty("then") List<ConsequentDefinition> conclusions) {}

    record AntecedentDefinition(
            String variable,
            String term,
            List<AntecedentDefinition> and,
            List<AntecedentDefinition> or,
            AntecedentDefinition not) {}

    record ConsequentDefinition(String variable, String term) {}
}
