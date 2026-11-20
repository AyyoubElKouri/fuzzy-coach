# Rule design

Rules are written as text and parsed once into a condition tree. This replaces
the old flat list of conditions plus one global `AND`/`OR` operator, which could
not express mixed operators or grouping.

## The rule text

```text
IF motivation is high AND (stress is low OR focus is high) THEN intensity is medium
```

`if`, `then`, `and`, `or`, and `is` are case-insensitive and reserved. Names are
case-sensitive and match `[A-Za-z_][A-Za-z0-9_]*`.

`AND` binds tighter than `OR`, and parentheses override precedence. A rule
without an operator is valid:

```text
IF motivation is low THEN intensity is low
```

## The model

```text
Rule
 |- Condition (sealed)
 |   |- SimpleCondition(variableName, termName)
 |   |- AndCondition(List<Condition>)
 |   `- OrCondition(List<Condition>)
 `- Conclusion(variableName, termName)
```

`Rule` is final and has one public constructor:

```java
Rule rule = new Rule("IF motivation is high AND stress is low THEN intensity is high");
```

It exposes `condition()` and `conclusion()`. `RuleParser` owns the grammar;
`Rule` only delegates to it.

`Conclusion` is not a `Condition`. A conclusion is a rule target, not a
proposition. Keeping it outside the sealed hierarchy prevents it from being used
inside a premise.

## Evaluating a condition

`Condition` has one method:

```java
double degree(ToDoubleFunction<SimpleCondition> lookup);
```

Each node implements it:

- `SimpleCondition` asks the lookup for its degree.
- `AndCondition` returns the minimum of its children.
- `OrCondition` returns the maximum of its children.

Inference supplies the lookup and calls `rule.condition().degree(...)`. It does
not inspect node types and does not choose between `min` and `max`. Adding an
operator means adding a node type, not editing inference.

`Condition.simpleConditions()` flattens a tree into its leaves, left to right.
Validation uses it to check every variable and term a rule references.

## Parsing

Recursive descent:

```text
rule          := IF orExpression THEN conclusion EOF
orExpression  := andExpression (OR andExpression)*
andExpression := atom (AND atom)*
atom          := condition | '(' orExpression ')'
condition     := IDENTIFIER IS IDENTIFIER
conclusion    := IDENTIFIER IS IDENTIFIER
```

Nested nodes with the same operator are flattened, so `a AND (b AND c)` becomes
one `AndCondition` with three children. A single leaf is returned without a
wrapper.

## Errors

`RuleSyntaxException` extends `IllegalArgumentException`, so existing catch
blocks keep working. The message shows the offset, the rule text, and a caret:

```text
Rule syntax error at offset 22: expected 'THEN'
IF motivation is high intensity is low
                      ^
```

## Why these choices

- Text in, tree out: the rule language stays small, and the parser is the only
  place that understands syntax.
- One polymorphic `degree`: inference no longer encodes fuzzy logic per
  operator.
- A separate parser: grammar changes do not touch `Rule`.
- A sealed `Condition`: the node types are closed and switches stay exhaustive.
  The hierarchy can be opened when a new operator is needed.
- `Rule.equals` and `hashCode` compare the tree and the conclusion structurally,
  so rules work in sets and in tests.

## Vocabulary

A linguistic variable exposes `terms()`, a map from term name to membership
function. A rule names one term per variable, for example `motivation is high`.

## Not included yet

`NOT`, multiple conclusions, and multi-level inference are out of scope.
