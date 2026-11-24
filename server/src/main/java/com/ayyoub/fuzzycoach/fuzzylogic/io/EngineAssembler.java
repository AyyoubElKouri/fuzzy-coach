package com.ayyoub.fuzzycoach.fuzzylogic.io;

import com.ayyoub.fuzzycoach.fuzzylogic.FuzzyLogicEngine;
import com.ayyoub.fuzzycoach.fuzzylogic.builder.FuzzyEngineBuilder;
import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.Bisector;
import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.Centroid;
import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.Defuzzifier;
import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.MeanOfMax;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.InvalidRuleException;
import com.ayyoub.fuzzycoach.fuzzylogic.inference.Inference;
import com.ayyoub.fuzzycoach.fuzzylogic.inference.Mamdani;
import com.ayyoub.fuzzycoach.fuzzylogic.inference.Sugeno;
import com.ayyoub.fuzzycoach.fuzzylogic.inference.Tsukamoto;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.Gaussian;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.MembershipFunction;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.Sigmoid;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.Trapezoidal;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.Triangular;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Complement;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Maximum;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Minimum;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Product;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.SNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.TNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Antecedent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Consequent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Turns a validated configuration into a working engine.
 */
final class EngineAssembler {

    private static final int DEFAULT_RESOLUTION = 100;

    private EngineAssembler() {}

    static FuzzyLogicEngine assemble(EngineDefinition definition) {
        if (definition == null) {
            throw new ConfigurationException("Definition is required");
        }

        Map<String, LinguisticVariable> inputs = variables(definition.inputs(), "input");
        Map<String, LinguisticVariable> outputs = variables(definition.outputs(), "output");

        FuzzyEngineBuilder builder = FuzzyEngineBuilder.builder();
        inputs.values().forEach(builder::input);
        outputs.values().forEach(builder::output);

        for (EngineDefinition.RuleDefinition rule : rules(definition.rules())) {
            builder.rule(rule(rule, inputs, outputs));
        }

        return builder.inference(inference(definition.inference())).build();
    }

    private static Map<String, LinguisticVariable> variables(
            List<EngineDefinition.VariableDefinition> definitions, String role) {
        if (definitions == null || definitions.isEmpty()) {
            throw new ConfigurationException("At least one " + role + " variable is required");
        }

        Map<String, LinguisticVariable> variables = new LinkedHashMap<>();
        for (EngineDefinition.VariableDefinition definition : definitions) {
            LinguisticVariable variable = variable(definition, role);

            if (variables.putIfAbsent(variable.name(), variable) != null) {
                throw new ConfigurationException("Duplicate variable: " + variable.name());
            }
        }

        return variables;
    }

    private static LinguisticVariable variable(
            EngineDefinition.VariableDefinition definition, String role) {
        if (definition == null) {
            throw new ConfigurationException(role + " variable definition is required");
        }

        try {
            String name = require(definition.name(), "Variable name is required");
            double minimum =
                    require(definition.minimum(), "Minimum is required for variable: " + name);
            double maximum =
                    require(definition.maximum(), "Maximum is required for variable: " + name);
            LinguisticVariable variable = new LinguisticVariable(name, minimum, maximum);

            if (definition.terms() == null || definition.terms().isEmpty()) {
                throw new ConfigurationException(
                        "At least one term is required for variable: " + name);
            }

            for (EngineDefinition.TermDefinition term : definition.terms()) {
                variable.addTerm(term(term, name));
            }

            return variable;
        } catch (IllegalArgumentException error) {
            throw new ConfigurationException(error.getMessage(), error);
        }
    }

    private static Term term(EngineDefinition.TermDefinition definition, String variable) {
        if (definition == null) {
            throw new ConfigurationException(
                    "Term definition is required for variable: " + variable);
        }

        String name = require(definition.name(), "Term name is required for variable: " + variable);

        return new Term(name, membership(definition.membership(), name));
    }

    private static MembershipFunction membership(
            EngineDefinition.MembershipDefinition definition, String term) {
        if (definition == null) {
            throw new ConfigurationException("Membership is required for term: " + term);
        }

        String type = require(definition.type(), "Membership type is required for term: " + term)
                .toLowerCase(Locale.ROOT);

        return switch (type) {
            case "triangular" -> {
                List<Double> parameters = parameters(definition, 3, type, term);
                yield new Triangular(parameters.get(0), parameters.get(1), parameters.get(2));
            }
            case "trapezoidal" -> {
                List<Double> parameters = parameters(definition, 4, type, term);
                yield new Trapezoidal(
                        parameters.get(0), parameters.get(1), parameters.get(2), parameters.get(3));
            }
            case "gaussian" -> {
                List<Double> parameters = parameters(definition, 2, type, term);
                yield new Gaussian(parameters.get(0), parameters.get(1));
            }
            case "sigmoid" -> {
                List<Double> parameters = parameters(definition, 2, type, term);
                yield new Sigmoid(parameters.get(0), parameters.get(1));
            }
            default -> throw new ConfigurationException("Unknown membership type: " + type);
        };
    }

    private static List<Double> parameters(
            EngineDefinition.MembershipDefinition definition,
            int expected,
            String type,
            String term) {
        List<Double> parameters = definition.parameters();

        if (parameters == null || parameters.size() != expected) {
            throw new ConfigurationException("Membership '"
                    + type
                    + "' for term '"
                    + term
                    + "' requires "
                    + expected
                    + " parameters");
        }

        for (Double parameter : parameters) {
            if (parameter == null) {
                throw new ConfigurationException(
                        "Membership '" + type + "' for term '" + term + "' has a null parameter");
            }
        }

        return parameters;
    }

    private static List<EngineDefinition.RuleDefinition> rules(
            List<EngineDefinition.RuleDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            throw new ConfigurationException("At least one rule is required");
        }

        return definitions;
    }

    private static Rule rule(
            EngineDefinition.RuleDefinition definition,
            Map<String, LinguisticVariable> inputs,
            Map<String, LinguisticVariable> outputs) {
        if (definition == null) {
            throw new InvalidRuleException("Rule definition is required");
        }

        try {
            Antecedent antecedent = antecedent(definition.condition(), inputs);
            List<EngineDefinition.ConsequentDefinition> conclusions = definition.conclusions();

            if (conclusions == null || conclusions.isEmpty()) {
                throw new InvalidRuleException("At least one conclusion is required");
            }

            Consequent consequent = null;
            for (EngineDefinition.ConsequentDefinition conclusion : conclusions) {
                if (conclusion == null) {
                    throw new InvalidRuleException("Conclusion is required");
                }

                String name = requireRule(conclusion.variable(), "Conclusion variable is required");
                LinguisticVariable variable = outputs.get(name);

                if (variable == null) {
                    throw new InvalidRuleException("Unknown output variable: " + name);
                }

                Term term = variable.term(
                        requireRule(conclusion.term(), "Conclusion term is required"));
                consequent = consequent == null
                        ? new Consequent(variable, term)
                        : consequent.and(variable, term);
            }

            return new Rule(antecedent, consequent);
        } catch (IllegalArgumentException error) {
            throw new InvalidRuleException(error.getMessage(), error);
        }
    }

    private static Antecedent antecedent(
            EngineDefinition.AntecedentDefinition definition,
            Map<String, LinguisticVariable> inputs) {
        if (definition == null) {
            throw new InvalidRuleException("Rule condition is required");
        }

        boolean proposition = definition.variable() != null || definition.term() != null;
        boolean and = definition.and() != null;
        boolean or = definition.or() != null;
        boolean not = definition.not() != null;

        if (proposition && (and || or || not)) {
            throw new InvalidRuleException(
                    "A rule condition is either a proposition, and, or, or not");
        }

        if (proposition) {
            String name = requireRule(definition.variable(), "Antecedent variable is required");
            String term = requireRule(definition.term(), "Antecedent term is required");
            LinguisticVariable variable = inputs.get(name);

            if (variable == null) {
                throw new InvalidRuleException("Unknown input variable: " + name);
            }

            return Antecedent.of(variable, variable.term(term));
        }

        if (and) {
            return combine(definition.and(), inputs, true);
        }

        if (or) {
            return combine(definition.or(), inputs, false);
        }

        if (not) {
            return Antecedent.not(antecedent(definition.not(), inputs));
        }

        throw new InvalidRuleException(
                "Rule condition must contain a proposition, and, or, or not");
    }

    private static Antecedent combine(
            List<EngineDefinition.AntecedentDefinition> operands,
            Map<String, LinguisticVariable> inputs,
            boolean and) {
        if (operands.size() < 2) {
            throw new InvalidRuleException("A combination requires at least two operands");
        }

        Antecedent result = antecedent(operands.getFirst(), inputs);

        for (int index = 1; index < operands.size(); index++) {
            Antecedent next = antecedent(operands.get(index), inputs);
            result = and ? Antecedent.and(result, next) : Antecedent.or(result, next);
        }

        return result;
    }

    private static Inference inference(EngineDefinition.InferenceDefinition definition) {
        EngineDefinition.InferenceDefinition config = definition == null
                ? new EngineDefinition.InferenceDefinition(null, null, null, null, null, null)
                : definition;

        try {
            String type = optional(config.type(), "mamdani").toLowerCase(Locale.ROOT);
            TNorm and = and(config.and());
            SNorm or = or(config.or());
            Complement complement = new Complement();

            return switch (type) {
                case "mamdani" ->
                    new Mamdani(and, or, complement, defuzzifier(config.defuzzifier()));
                case "sugeno" -> new Sugeno(and, or, complement);
                case "tsukamoto" ->
                    new Tsukamoto(and, or, complement, resolution(config.resolution()));
                default -> throw new ConfigurationException("Unknown inference type: " + type);
            };
        } catch (IllegalArgumentException error) {
            throw new ConfigurationException(error.getMessage(), error);
        }
    }

    private static TNorm and(String name) {
        return switch (optional(name, "minimum").toLowerCase(Locale.ROOT)) {
            case "minimum" -> new Minimum();
            case "product" -> new Product();
            default -> throw new ConfigurationException("Unknown and operator: " + name);
        };
    }

    private static SNorm or(String name) {
        return switch (optional(name, "maximum").toLowerCase(Locale.ROOT)) {
            case "maximum" -> new Maximum();
            default -> throw new ConfigurationException("Unknown or operator: " + name);
        };
    }

    private static Defuzzifier defuzzifier(String name) {
        return switch (optional(name, "centroid").toLowerCase(Locale.ROOT)) {
            case "centroid" -> new Centroid();
            case "bisector" -> new Bisector();
            case "meanofmax" -> new MeanOfMax();
            default -> throw new ConfigurationException("Unknown defuzzifier: " + name);
        };
    }

    private static int resolution(Integer resolution) {
        return resolution == null ? DEFAULT_RESOLUTION : resolution;
    }

    private static String require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(message);
        }

        return value;
    }

    private static double require(Double value, String message) {
        if (value == null) {
            throw new ConfigurationException(message);
        }

        return value;
    }

    private static String requireRule(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidRuleException(message);
        }

        return value;
    }

    private static String optional(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
