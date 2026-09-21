# R18. Logging

Load when adding log statements in infrastructure or application layers.

## MUST

- Infrastructure and application layers use:

```java
private static final Logger log = Loggers.getLogger(<ClassName>.class);
```
