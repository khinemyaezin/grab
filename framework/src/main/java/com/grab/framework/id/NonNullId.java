package com.grab.framework.id;

import java.io.Serializable;

public interface NonNullId<T> extends Serializable {
    T getValue();
}
