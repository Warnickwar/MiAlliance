package com.mialliance.mind.sensors;

import com.mialliance.mind.agents.MindOwner;
import com.mialliance.mind.events.IEvent;
import com.mialliance.mind.events.IEventListener;
import com.mialliance.mind.events.SenseEvent;
import org.jetbrains.annotations.NotNull;

public abstract class EventSensor<O extends MindOwner, E extends IEvent> extends BaseSensor<O> implements IEventListener<E> {

    private final Class<E> eventType;
    protected final SenseEvent<E> onSense;

    public EventSensor(@NotNull O owner, Class<E> eventType, SenseEvent<E> onSense) {
        super(owner);
        this.eventType = eventType;
        this.onSense = onSense;
    }

    @Override
    public void tick() {}

    @Override
    protected void register() {
        this.getOwner().getAgent().registerListener(eventType, this);
    }

    @Override
    protected void unregister() {
        this.getOwner().getAgent().removeListener(eventType, this);
    }

}
