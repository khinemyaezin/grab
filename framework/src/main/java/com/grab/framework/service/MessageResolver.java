package com.grab.framework.service;

import java.util.Map;

public interface MessageResolver {
    String resolve(String code, Map<String, Object> args, String locale);
}
