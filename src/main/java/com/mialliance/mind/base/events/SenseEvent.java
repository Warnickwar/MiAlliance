package com.mialliance.mind.base.events;

public interface SenseEvent<T extends IEvent> {
    void accept(T event);
}
