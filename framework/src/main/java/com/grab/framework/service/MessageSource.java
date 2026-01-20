package com.grab.framework.service;

import java.util.Map;

public interface MessageSource {
    String code();
    Map<String, Object> args();
}