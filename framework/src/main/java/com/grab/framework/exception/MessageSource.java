package com.grab.framework.exception;

import java.util.Map;

public interface MessageSource {
    ErrorCategory kind();
    String code();
    Map<String, Object> args();
}