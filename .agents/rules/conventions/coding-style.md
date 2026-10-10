---
trigger: always_on
---

# R20. Coding Style

Load when writing or refactoring Java in this repo.

## MUST

- Extract intermediate variables.
- Use descriptive names for those intermediates.

## MUST NOT

- Nest function invocations, for example `doSomething(doA(doB()))`.
- Do not add comments.
- Do not use inline package name, instead use import, for example `com.example.Child`
- Adhere to strict explicit typing. Do not use var anywhere in the Java code.