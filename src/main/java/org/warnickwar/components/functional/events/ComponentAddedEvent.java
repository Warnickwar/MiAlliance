package org.warnickwar.components.functional.events;

import org.warnickwar.components.functional.ComponentType;

public class ComponentAddedEvent extends ComponentEvent {

    private final ComponentType<?,?> type;
    private final boolean locked;

    public ComponentAddedEvent(ComponentType<?,?> type, boolean locked) {
        this.type = type;
        this.locked = locked;
    }

    public ComponentType<?,?> getType() {
        return type;
    }

    public boolean isLocked() {
        return locked;
    }
}
