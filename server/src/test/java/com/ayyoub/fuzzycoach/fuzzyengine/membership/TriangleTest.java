package com.ayyoub.fuzzycoach.fuzzyengine.membership;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TriangleTest {

    private final Triangle triangle = new Triangle(0, 5, 10);

    @Test
    void peakIsOne() {
        assertEquals(1.0, triangle.degree(5), 1e-9);
    }

    @Test
    void feetAndOutsideAreZero() {
        assertEquals(0.0, triangle.degree(0), 1e-9);
        assertEquals(0.0, triangle.degree(10), 1e-9);
        assertEquals(0.0, triangle.degree(-1), 1e-9);
        assertEquals(0.0, triangle.degree(11), 1e-9);
    }

    @Test
    void slopesAreLinear() {
        assertEquals(0.5, triangle.degree(2.5), 1e-9);
        assertEquals(0.5, triangle.degree(7.5), 1e-9);
        assertEquals(0.2, triangle.degree(1), 1e-9);
        assertEquals(0.8, triangle.degree(6), 1e-9);
    }

    @Test
    void supportsShoulderShapes() {
        assertEquals(0.5, new Triangle(0, 0, 5).degree(2.5), 1e-9);
        assertEquals(0.5, new Triangle(5, 10, 10).degree(7.5), 1e-9);
    }

    @Test
    void rejectsInvalidPoints() {
        assertThrows(IllegalArgumentException.class, () -> new Triangle(5, 3, 10));
        assertThrows(IllegalArgumentException.class, () -> new Triangle(0, 5, 3));
        assertThrows(IllegalArgumentException.class, () -> new Triangle(0, 0, 0));
    }
}
