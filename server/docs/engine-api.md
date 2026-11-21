# Engine API

How to define a fuzzy system, evaluate it, and read the result.

## Define the variables

```java
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
```

Terms keep their declaration order. The strict `new LinguisticVariable(...)`
record constructor is still available for code that already has a map.

## Build the engine

```java
FuzzyEngine engine = FuzzyEngine.builder()
    .input(motivation)
    .input(stress)
    .output(intensity)
    .rules(
        "IF motivation is low THEN intensity is low",
        "IF motivation is high AND stress is low THEN intensity is high",
        "IF motivation is high AND stress is high THEN intensity is medium")
    .build();
```

The builder defaults to `CentroidDefuzzifier`, `DefaultFuzzifier`, and
`DefaultInference`. Override them with `.defuzzifier(...)`, `.fuzzifier(...)`, and
`.inference(...)`.

Rules can also be added as objects with `.rule(rule)` or `.rules(rule1, rule2)`.
When a text rule fails to parse, `.rules(String...)` names the failing rule, for
example `rule 2 of 3: expected 'THEN'`.

## Evaluate

```java
FuzzyResult result = engine.evaluate(
    Inputs.of("motivation", 8.0)
        .with("stress", 2.0));
```

`evaluate(Map<String, Double>)` remains available for callers that already have
a map. Missing and unknown input variables fail before any fuzzification runs.

## Read the result

```java
double value = result.output("intensity");

OptionalDouble maybeValue = result.findOutput("intensity");

FuzzyValue fuzzy = result.fuzzyValue("intensity");
Map<String, Double> degrees = fuzzy.degrees();
```

To explain the result, ask which rules fired for an output:

```java
for (RuleActivation activation : result.explanation("intensity")) {
    System.out.println(activation.rule() + " fired at " + activation.activation());
}
```

`explanation` returns only rules with an activation above zero, strongest
first. The rule itself exposes the conclusion that contributed.

## Reuse and safety

- Build the engine once, then evaluate it many times.
- The engine is immutable and safe to share between threads.
- Variable references and rule syntax are validated at build time, so a typo
  fails early instead of during a request.
