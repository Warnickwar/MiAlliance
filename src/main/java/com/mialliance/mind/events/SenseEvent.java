package com.mialliance.mind.events;

public interface SenseEvent<T extends IEvent> {
    void accept(T event);
}
