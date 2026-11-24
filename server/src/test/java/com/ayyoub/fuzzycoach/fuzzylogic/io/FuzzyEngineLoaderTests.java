package com.ayyoub.fuzzycoach.fuzzylogic.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ayyoub.fuzzycoach.fuzzylogic.FuzzyLogicEngine;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.InvalidRuleException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FuzzyEngineLoaderTests {

    private static final String MAMDANI = """
            {
              "inference": {
                "type": "mamdani",
                "and": "minimum",
                "or": "maximum",
                "not": "standard",
                "defuzzifier": "centroid"
              },
              "inputs": [
                {
                  "name": "motivation",
                  "minimum": 0,
                  "maximum": 10,
                  "terms": [
                    {"name": "low", "membership": {"type": "triangular", "parameters": [0, 0, 5]}},
                    {"name": "high", "membership": {"type": "triangular", "parameters": [5, 10, 10]}}
                  ]
                },
                {
                  "name": "stress",
                  "minimum": 0,
                  "maximum": 10,
                  "terms": [
                    {"name": "low", "membership": {"type": "triangular", "parameters": [0, 0, 5]}},
                    {"name": "high", "membership": {"type": "triangular", "parameters": [5, 10, 10]}}
                  ]
                }
              ],
              "outputs": [
                {
                  "name": "intensity",
                  "minimum": 0,
                  "maximum": 10,
                  "terms": [
                    {"name": "low", "membership": {"type": "triangular", "parameters": [0, 0, 5]}},
                    {"name": "high", "membership": {"type": "triangular", "parameters": [5, 10, 10]}}
                  ]
                }
              ],
              "rules": [
                {
                  "if": {
                    "and": [
                      {"variable": "motivation", "term": "high"},
                      {"variable": "stress", "term": "low"}
                    ]
                  },
                  "then": [{"variable": "intensity", "term": "high"}]
                },
                {
                  "if": {
                    "or": [
                      {"variable": "motivation", "term": "low"},
                      {"variable": "stress", "term": "high"}
                    ]
                  },
                  "then": [{"variable": "intensity", "term": "low"}]
                }
              ]
            }
            """;

    @Test
    void loadsMamdaniEngineFromFile(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("coach.json");
        Files.writeString(file, MAMDANI);

        FuzzyLogicEngine engine = FuzzyEngineLoader.load(file);
        engine.setVariable("motivation", 7.0);
        engine.setVariable("stress", 3.0);
        engine.evaluate();

        assertEquals(7.9329113924050585, engine.output("intensity"), 1e-9);
        assertEquals(1, engine.outputs().size());
    }

    @Test
    void appliesNotToAntecedent() {
        FuzzyLogicEngine engine = FuzzyEngineLoader.load(MAMDANI.replace(
                "{\"variable\": \"stress\", \"term\": \"low\"}",
                "{\"not\": {\"variable\": \"stress\", \"term\": \"high\"}}"));

        engine.setVariable("motivation", 7.0);
        engine.setVariable("stress", 3.0);
        engine.evaluate();

        assertTrue(engine.output("intensity") >= 0.0);
    }

    @Test
    void loadsSugenoInference() {
        FuzzyLogicEngine engine =
                FuzzyEngineLoader.load(MAMDANI.replace("\"mamdani\"", "\"sugeno\""));

        engine.setVariable("motivation", 9.5);
        engine.setVariable("stress", 2.5);
        engine.evaluate();

        assertEquals(8.333333333333334, engine.output("intensity"), 0.05);
    }

    @Test
    void loadsTsukamotoInference() {
        FuzzyLogicEngine engine =
                FuzzyEngineLoader.load(MAMDANI.replace("\"mamdani\"", "\"tsukamoto\""));

        engine.setVariable("motivation", 9.5);
        engine.setVariable("stress", 2.5);
        engine.evaluate();

        assertEquals(7.5, engine.output("intensity"), 0.1);
    }

    @Test
    void rejectsUnknownMembershipType() {
        String config = MAMDANI.replace(
                "\"triangular\", \"parameters\": [0, 0, 5]", "\"bell\", \"parameters\": [0, 1, 2]");

        ConfigurationException error =
                assertThrows(ConfigurationException.class, () -> FuzzyEngineLoader.load(config));
        assertTrue(error.getMessage().contains("Unknown membership type: bell"));
    }

    @Test
    void rejectsUnknownInputVariable() {
        String config = MAMDANI.replace(
                "\"variable\": \"stress\", \"term\": \"low\"",
                "\"variable\": \"energy\", \"term\": \"low\"");

        InvalidRuleException error =
                assertThrows(InvalidRuleException.class, () -> FuzzyEngineLoader.load(config));
        assertTrue(error.getMessage().contains("Unknown input variable: energy"));
    }

    @Test
    void rejectsUnknownProperty() {
        String config = MAMDANI.replace("\"type\": \"mamdani\"", "\"kind\": \"mamdani\"");

        assertThrows(ConfigurationException.class, () -> FuzzyEngineLoader.load(config));
    }
}
