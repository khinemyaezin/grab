package com.grab.framework.workflow;

@FunctionalInterface
public interface ChoiceSelector<I> {

    String select(I input);
}
