package com.mialliance.mind.events;

import org.jetbrains.annotations.NotNull;

public interface IEventListener<T extends IEvent> {

    void onEvent(@NotNull T event);

}
