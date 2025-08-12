package com.mialliance.mind.events;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventManager {

    private final Map<Class<? extends IEvent>, HashSet<IEventListener<?>>> listeners;

    public EventManager() {
        this.listeners = new HashMap<>();
    }

    public <T extends IEvent> boolean registerListener(@NotNull Class<T> type, @NotNull IEventListener<T> listener) {
        return listeners.computeIfAbsent(type, (key)-> new HashSet<>()).add(listener);
    }

    public <T extends IEvent> boolean unregisterListener(@NotNull Class<T> type, @NotNull IEventListener<T> listener) {
        return listeners.containsKey(type) && listeners.get(type).remove(listener);
    }

    public <T extends IEvent> boolean unregisterListener(@NotNull IEventListener<T> listener) {
        AtomicBoolean bool = new AtomicBoolean(false);
        listeners.forEach((type, set) -> bool.set(bool.get() | set.remove(listener)));
        return bool.get();
    }

    @SuppressWarnings("unchecked")
    public <T extends IEvent> void call(T event) {
        Class<T> eventType = (Class<T>) event.getClass();

        if (!listeners.containsKey(eventType)) return;

        listeners.get(eventType).forEach(listener -> ((IEventListener<T>) listener).onEvent(event));
    }
}
