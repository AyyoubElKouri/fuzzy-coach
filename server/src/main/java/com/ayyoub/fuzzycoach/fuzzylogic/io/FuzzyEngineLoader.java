package com.ayyoub.fuzzycoach.fuzzylogic.io;

import com.ayyoub.fuzzycoach.fuzzylogic.FuzzyLogicEngine;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Reads a JSON engine configuration and returns a ready {@link FuzzyLogicEngine}.
 *
 * <p>The document has four top-level fields: {@code inference}, {@code inputs}, {@code outputs},
 * and {@code rules}.
 *
 * <pre>{@code
 * {
 *   "inference": {
 *     "type": "mamdani",
 *     "and": "minimum",
 *     "or": "maximum",
 *     "not": "standard",
 *     "defuzzifier": "centroid",
 *     "resolution": 100
 *   },
 *   "inputs": [
 *     {
 *       "name": "motivation",
 *       "minimum": 0,
 *       "maximum": 10,
 *       "terms": [
 *         {"name": "low", "membership": {"type": "triangular", "parameters": [0, 0, 5]}},
 *         {"name": "high", "membership": {"type": "triangular", "parameters": [5, 10, 10]}}
 *       ]
 *     }
 *   ],
 *   "outputs": [
 *     {
 *       "name": "intensity",
 *       "minimum": 0,
 *       "maximum": 10,
 *       "terms": [
 *         {"name": "low", "membership": {"type": "triangular", "parameters": [0, 0, 5]}},
 *         {"name": "high", "membership": {"type": "triangular", "parameters": [5, 10, 10]}}
 *       ]
 *     }
 *   ],
 *   "rules": [
 *     {
 *       "if": {"variable": "motivation", "term": "high"},
 *       "then": [{"variable": "intensity", "term": "high"}]
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <p>Membership types are {@code triangular}, {@code trapezoidal}, {@code gaussian}, and {@code
 * sigmoid}. Inference types are {@code mamdani}, {@code sugeno}, and {@code tsukamoto}. The {@code
 * and} operator is {@code minimum} or {@code product}; the {@code or} operator is {@code maximum};
 * the {@code not} operator is {@code standard}. Mamdani defuzzifiers are {@code centroid}, {@code
 * bisector}, and {@code meanofmax}.
 *
 * <p>Rule conditions are propositions ({@code variable} and {@code term}), or a list of operands
 * under {@code and} or {@code or}, or a single operand under {@code not}.
 */
public final class FuzzyEngineLoader {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private FuzzyEngineLoader() {}

    /**
     * Reads a configuration file.
     *
     * @param path path to a JSON configuration
     * @return a configured engine
     * @throws IOException when the file cannot be read
     */
    public static FuzzyLogicEngine load(Path path) throws IOException {
        Objects.requireNonNull(path, "Path is required");

        try (InputStream input = Files.newInputStream(path)) {
            return load(input);
        }
    }

    /**
     * Reads a configuration from a stream. The stream is not closed.
     *
     * @param input JSON configuration
     * @return a configured engine
     */
    public static FuzzyLogicEngine load(InputStream input) {
        Objects.requireNonNull(input, "Input stream is required");

        return read(() -> MAPPER.readValue(input, EngineDefinition.class));
    }

    /**
     * Reads a configuration from a JSON string.
     *
     * @param json JSON configuration
     * @return a configured engine
     */
    public static FuzzyLogicEngine load(String json) {
        Objects.requireNonNull(json, "JSON is required");

        return read(() -> MAPPER.readValue(json, EngineDefinition.class));
    }

    private static FuzzyLogicEngine read(Supplier<EngineDefinition> supplier) {
        try {
            return EngineAssembler.assemble(supplier.get());
        } catch (JacksonException error) {
            throw new ConfigurationException(
                    "Invalid engine configuration: " + error.getMessage(), error);
        }
    }
}
