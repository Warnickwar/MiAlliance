package com.mialliance.mind.base.sensors;

import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.events.IEvent;
import com.mialliance.mind.base.events.IEventListener;
import com.mialliance.mind.base.events.SenseEvent;
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
    public boolean shouldTick() {
        return false;
    }

    @Override
    public void onTick() {}

    @Override
    protected void register() {
        this.getOwner().getAgent().registerListener(eventType, this);
    }

    @Override
    protected void unregister() {
        this.getOwner().getAgent().removeListener(eventType, this);
    }

}
