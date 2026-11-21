package com.ayyoub.fuzzycoach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ayyoub.fuzzycoach.fuzzyengine.engine.FuzzyEngine;
import com.ayyoub.fuzzycoach.fuzzyengine.engine.FuzzyResult;
import com.ayyoub.fuzzycoach.fuzzyengine.engine.Inputs;
import com.ayyoub.fuzzycoach.fuzzyengine.membership.Triangle;
import com.ayyoub.fuzzycoach.fuzzyengine.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.variable.LinguisticVariable;

@SpringBootApplication
public class FuzzycoachApplication {

	static void main(String[] args) {
		
		LinguisticVariable motivation = LinguisticVariable
			.named("motivation")
			.range(0, 10)
			.term("low", new Triangle(0, 0, 5))
			.term("high", new Triangle(5, 10, 10))
			.build();

		LinguisticVariable stress = LinguisticVariable
			.named("stress")
			.range(0, 10)
			.term("low", new Triangle(0, 0, 5))
			.term("high", new Triangle(5, 10, 10))
			.build();

		LinguisticVariable intensity = LinguisticVariable
			.named("intensity")
			.range(0, 10)
			.term("low", new Triangle(0, 0, 5))
			.term("medium", new Triangle(2.5, 5, 7.5))
			.term("high", new Triangle(5, 10, 10))
			.build();

		
		Rule rule1 = new Rule("IF motivation is high AND stress is low THEN intensity is high");
		Rule rule2 = new Rule("IF motivation is low OR stress is high THEN intensity is low");
		Rule rule3 = new Rule("IF motivation is high AND (stress is low OR stress is high) THEN intensity is medium");
		Rule rule4 = new Rule("IF motivation is low AND stress is low THEN intensity is medium");
		
		
		FuzzyEngine engine = FuzzyEngine.builder()
			.input(motivation)
			.input(stress)
			.output(intensity)
			.rule(rule1)
			.rule(rule2)
			.rule(rule3)
			.rule(rule4)
			.build();
			
		FuzzyResult result = engine.evaluate(
			Inputs.of("motivation", 7.0).with("stress", 3.0)
		);
		
		System.out.println(result.output("intensity"));
		
		//SpringApplication.run(FuzzycoachApplication.class, args);
	}

	
}
