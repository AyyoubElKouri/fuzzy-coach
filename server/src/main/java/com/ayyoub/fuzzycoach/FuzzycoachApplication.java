package com.ayyoub.fuzzycoach;

import com.ayyoub.fuzzycoach.fuzzylogic.FuzzyLogicEngine;
import com.ayyoub.fuzzycoach.fuzzylogic.builder.FuzzyEngineBuilder;
import com.ayyoub.fuzzycoach.fuzzylogic.inference.Mamdani;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.Triangular;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Antecedent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FuzzycoachApplication {

    static void main(String[] args) {
        LinguisticVariable motivation = LinguisticVariable.builder()
                .named("motivation")
                .range(0, 10)
                .term("low", new Triangular(0, 0, 5))
                .term("high", new Triangular(5, 10, 10))
                .build();

        LinguisticVariable stress = LinguisticVariable.builder()
                .named("stress")
                .range(0, 10)
                .term("low", new Triangular(0, 0, 5))
                .term("high", new Triangular(5, 10, 10))
                .build();

        LinguisticVariable intensity = LinguisticVariable.builder()
                .named("intensity")
                .range(0, 10)
                .term("low", new Triangular(0, 0, 5))
                .term("high", new Triangular(5, 10, 10))
                .build();

        Rule highIntensity = Rule.builder()
                .when(Antecedent.and(
                        Antecedent.of(motivation, motivation.term("high")),
                        Antecedent.of(stress, stress.term("low"))))
                .then(intensity, intensity.term("high"))
                .build();
        Rule lowIntensity = Rule.builder()
                .when(Antecedent.or(
                        Antecedent.of(motivation, motivation.term("low")),
                        Antecedent.of(stress, stress.term("high"))))
                .then(intensity, intensity.term("low"))
                .build();

        FuzzyLogicEngine engine = FuzzyEngineBuilder.builder()
                .input(motivation)
                .input(stress)
                .output(intensity)
                .rules(highIntensity, lowIntensity)
                .inference(new Mamdani())
                .build();

        engine.setVariable("motivation", 8.0);
        engine.setVariable("stress", 2.0);
        engine.evaluate();

        System.out.println("intensity = " + engine.output("intensity"));
        System.out.println("outputs   = " + engine.outputs());

        SpringApplication.run(FuzzycoachApplication.class, args);
    }
}
