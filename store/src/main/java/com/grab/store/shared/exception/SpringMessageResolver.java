package com.grab.store.shared.exception;

import com.grab.framework.exception.MessageResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class SpringMessageResolver implements MessageResolver {

    private final org.springframework.context.MessageSource messageSource;

    public SpringMessageResolver(org.springframework.context.MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String resolve(String code, Map<String, Object> args, String locale) {
        if (code == null || code.isBlank()) {
            return null;
        }

        java.util.Locale resolvedLocale = locale == null || locale.isBlank()
                ? java.util.Locale.ENGLISH
                : java.util.Locale.forLanguageTag(locale);

        String template = messageSource.getMessage(code, null, code, resolvedLocale);
        if (args == null || args.isEmpty()) {
            return template;
        }

        String resolved = template;
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            resolved = resolved.replace(
                    "{" + entry.getKey() + "}",
                    Objects.toString(entry.getValue(), "null")
            );
        }
        return resolved;
    }
}
