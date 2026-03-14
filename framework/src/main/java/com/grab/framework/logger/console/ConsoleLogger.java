package com.grab.framework.logger.console;

import com.grab.framework.logger.LogLevel;
import com.grab.framework.logger.Logger;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class ConsoleLogger implements Logger {
    private final String name;
    private final LogLevel level;

    public ConsoleLogger(String name, LogLevel level) {
        this.name = Objects.requireNonNullElse(name, "unknown");
        this.level = Objects.requireNonNullElse(level, LogLevel.INFO);
    }

    private boolean isEnabled(LogLevel logLevel) {
        return logLevel.ordinal() >= this.level.ordinal();
    }

    private void log(LogLevel level, String message, Object... args) {
        if (!isEnabled(level)) {
            return;
        }

        Object[] effectiveArgs = args == null ? new Object[0] : args;
        Throwable throwable = trailingThrowable(message, effectiveArgs);
        if (throwable != null) {
            effectiveArgs = Arrays.copyOf(effectiveArgs, effectiveArgs.length - 1);
        }

        String rendered = formatMessage(message, effectiveArgs);
        System.out.printf("[%s] [%s] %s%n", level.name(), name, rendered);
        if (throwable != null) {
            throwable.printStackTrace(System.out);
        }
    }

    private void log(LogLevel level, String message, Throwable t) {
        if (!isEnabled(level)) {
            return;
        }

        System.out.printf("[%s] [%s] %s%n", level.name(), name, safeMessage(message));
        if (t != null) {
            t.printStackTrace(System.out);
        }
    }

    private Throwable trailingThrowable(String message, Object[] args) {
        if (args.length == 0) {
            return null;
        }
        Object last = args[args.length - 1];
        if (!(last instanceof Throwable throwable)) {
            return null;
        }
        return countPlaceholders(message) < args.length ? throwable : null;
    }

    private int countPlaceholders(String message) {
        String template = safeMessage(message);
        int count = 0;
        for (int i = 0; i < template.length() - 1; i++) {
            if (template.charAt(i) == '{' && template.charAt(i + 1) == '}') {
                count++;
                i++;
            }
        }
        return count;
    }

    private String formatMessage(String message, Object[] args) {
        String template = safeMessage(message);
        if (args.length == 0) {
            return template;
        }

        StringBuilder result = new StringBuilder(template.length() + (args.length * 16));
        int argIndex = 0;

        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            if (current == '{' && i + 1 < template.length() && template.charAt(i + 1) == '}') {
                if (argIndex < args.length) {
                    result.append(String.valueOf(args[argIndex++]));
                } else {
                    result.append("{}");
                }
                i++;
                continue;
            }
            result.append(current);
        }

        if (argIndex < args.length) {
            StringJoiner joiner = new StringJoiner(", ");
            for (int i = argIndex; i < args.length; i++) {
                joiner.add(String.valueOf(args[i]));
            }
            result.append(" [extraArgs=").append(joiner).append("]");
        }

        return result.toString();
    }

    private String safeMessage(String message) {
        return message == null ? "null" : message;
    }

    public boolean isTraceEnabled() { return isEnabled(LogLevel.TRACE); }
    public boolean isDebugEnabled() { return isEnabled(LogLevel.DEBUG); }
    public boolean isInfoEnabled()  { return isEnabled(LogLevel.INFO); }
    public boolean isWarnEnabled()  { return isEnabled(LogLevel.WARN); }
    public boolean isErrorEnabled() { return isEnabled(LogLevel.ERROR); }

    public void trace(String message)                      { log(LogLevel.TRACE, message); }
    public void trace(String message, Object... args)     { log(LogLevel.TRACE, message, args); }
    public void trace(String message, Throwable throwable){ log(LogLevel.TRACE, message, throwable); }

    public void debug(String message)                      { log(LogLevel.DEBUG, message); }
    public void debug(String message, Object... args)     { log(LogLevel.DEBUG, message, args); }
    public void debug(String message, Throwable throwable){ log(LogLevel.DEBUG, message, throwable); }

    public void info(String message)                       { log(LogLevel.INFO, message); }
    public void info(String message, Object... args)      { log(LogLevel.INFO, message, args); }
    public void info(String message, Throwable throwable) { log(LogLevel.INFO, message, throwable); }

    public void warn(String message)                       { log(LogLevel.WARN, message); }
    public void warn(String message, Object... args)      { log(LogLevel.WARN, message, args); }
    public void warn(String message, Throwable throwable) { log(LogLevel.WARN, message, throwable); }

    public void error(String message)                       { log(LogLevel.ERROR, message); }
    public void error(String message, Object... args)      { log(LogLevel.ERROR, message, args); }
    public void error(String message, Throwable throwable) { log(LogLevel.ERROR, message, throwable); }
}
