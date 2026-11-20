package com.ayyoub.fuzzycoach;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ayyoub.fuzzycoach.fuzzyengine.core.AndCondition;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Condition;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Conclusion;
import com.ayyoub.fuzzycoach.fuzzyengine.core.OrCondition;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.core.SimpleCondition;

@SpringBootApplication
public class FuzzycoachApplication {

	private static final String BRANCH = "+- ";
	private static final String LAST_BRANCH = "`- ";
	private static final String PIPE = "|  ";
	private static final String SPACE = "   ";

	public static void main(String[] args) {
		// Debug entry point for the Rule API only.
		List<Rule> rules = List.of(
			new Rule("IF motivation is low THEN intensity is low"),
			new Rule("IF motivation is high AND stress is low THEN intensity is high"),
			new Rule("IF motivation is high OR stress is low THEN intensity is high"),
			new Rule("IF motivation is high AND (stress is low OR motivation is low) "
				+ "THEN intensity is medium"));

		rules.forEach(FuzzycoachApplication::printRule);

		//SpringApplication.run(FuzzycoachApplication.class, args);
	}

	private static void printRule(Rule rule) {
		StringBuilder output = new StringBuilder();
		output.append("Rule").append(System.lineSeparator());

		printConditionNode("condition", rule.condition(), "", false, output);
		printConclusionNode("conclusion", rule.conclusion(), "", true, output);

		System.out.print(output);
		System.out.println();
	}

	private static void printConditionNode(
		String label,
		Condition condition,
		String prefix,
		boolean last,
		StringBuilder output) {

		output.append(prefix)
			.append(last ? LAST_BRANCH : BRANCH)
			.append(label)
			.append(": ")
			.append(condition.getClass().getSimpleName())
			.append(System.lineSeparator());

		String childPrefix = prefix + (last ? SPACE : PIPE);

		switch (condition) {
			case SimpleCondition simple ->
				printLeafFields(simple.variableName(), simple.termName(), childPrefix, output);
			case AndCondition and -> printConditionChildren(and.conditions(), childPrefix, output);
			case OrCondition or -> printConditionChildren(or.conditions(), childPrefix, output);
		}
	}

	private static void printConclusionNode(
		String label,
		Conclusion conclusion,
		String prefix,
		boolean last,
		StringBuilder output) {

		output.append(prefix)
			.append(last ? LAST_BRANCH : BRANCH)
			.append(label)
			.append(": Conclusion")
			.append(System.lineSeparator());

		String childPrefix = prefix + (last ? SPACE : PIPE);
		printLeafFields(conclusion.variableName(), conclusion.termName(), childPrefix, output);
	}

	private static void printConditionChildren(
		List<Condition> conditions,
		String prefix,
		StringBuilder output) {

		for (int index = 0; index < conditions.size(); index++) {
			boolean last = index == conditions.size() - 1;
			printConditionNode(
				"conditions[" + index + "]",
				conditions.get(index),
				prefix,
				last,
				output);
		}
	}

	private static void printLeafFields(
		String variableName,
		String termName,
		String prefix,
		StringBuilder output) {

		printLeaf("variableName", variableName, prefix, false, output);
		printLeaf("termName", termName, prefix, true, output);
	}

	private static void printLeaf(
		String name,
		String value,
		String prefix,
		boolean last,
		StringBuilder output) {

		output.append(prefix)
			.append(last ? LAST_BRANCH : BRANCH)
			.append(name)
			.append(": \"")
			.append(value)
			.append('"')
			.append(System.lineSeparator());
	}
}
