---
name: clean-coder
description: MANDATORY before writing, modifying, refactoring, or reviewing any code, in any language. Load this skill first, before creating or editing any source file, even for small changes, one-line fixes, scripts, or tests. Defines the naming, structure, readability, and validation rules all code must follow. Do not write code without loading this skill.
---

# Clean Coder

This skill defines a set of rules and best practices for writing clean, maintainable, and readable code. It is mandatory to load this skill before starting any coding task, regardless of the programming language or the size of the change.

## Code Structure

### Blank lines inside methods

- Put one blank line between each logical step of a method.
- Always put a blank line before the final `return`, unless the method is 3 lines or fewer.
- Never put blank lines at the start or end of a block, and never use more than one in a row.

```java
// Bad example
public double average(List<Double> values) {
    if (values == null || values.isEmpty()) {
        throw new IllegalArgumentException("Values are required");
    }
    double sum = 0;
    for (double value : values) {
        sum += value;
    }
    double result = sum / values.size();
    return result;
}
```

```java
// Good example
public double average(List<Double> values) {
    if (values == null || values.isEmpty()) {
        throw new IllegalArgumentException("Values are required");
    }

    double sum = 0;
    for (double value : values) {
        sum += value;
    }

    return sum / values.size();
}
```