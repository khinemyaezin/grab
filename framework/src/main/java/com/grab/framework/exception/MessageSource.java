package com.grab.framework.exception;

import java.io.Serializable;
import java.util.Map;

public interface MessageSource extends Serializable {
    ErrorCategory kind();
    String code();
    Map<String, Object> args();
}