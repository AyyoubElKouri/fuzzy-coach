package com.ayyoub.fuzzycoach.fuzzyengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class InputsTest {

    @Test
    void keepsTheInsertionOrder() {
        Inputs inputs = Inputs.of("motivation", 8.0).with("stress", 2.0);

        assertEquals(List.of("motivation", "stress"), List.copyOf(inputs.asMap().keySet()));
    }

    @Test
    void withReplacesAnExistingValue() {
        Inputs inputs = Inputs.of("motivation", 8.0).with("motivation", 3.0);

        assertEquals(3.0, inputs.asMap().get("motivation"), 1e-9);
        assertEquals(1, inputs.asMap().size());
    }

    @Test
    void rejectsNonFiniteValues() {
        assertThrows(IllegalArgumentException.class, () -> Inputs.of("motivation", Double.NaN));
    }

    @Test
    void mapIsReadOnly() {
        Inputs inputs = Inputs.of("motivation", 8.0);

        assertThrows(UnsupportedOperationException.class, () -> inputs.asMap().put("stress", 2.0));
    }

    @Test
    void fromCopiesTheSourceMap() {
        Map<String, Double> source = new LinkedHashMap<>();
        source.put("motivation", 8.0);

        Inputs inputs = Inputs.from(source);
        source.put("stress", 2.0);

        assertEquals(Map.of("motivation", 8.0), inputs.asMap());
    }
}
