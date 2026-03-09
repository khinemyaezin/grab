package com.grab.framework.exception;

import java.util.Map;

public interface MessageResolver {
    String resolve(String code, Map<String, Object> args, String locale);
}
