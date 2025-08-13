package org.warnickwar.components.functional.events;

import org.warnickwar.components.functional.ComponentType;

public class ToggledComponentEvent extends ComponentEvent {

    private final ComponentType<?,?> type;
    private final Boolean newState;

    public ToggledComponentEvent(ComponentType<?,?> type, Boolean newState) {
        this.type = type;
        this.newState = newState;
    }

    public ComponentType<?,?> getType() {
        return type;
    }

    public Boolean getNewState() {
        return newState;
    }
}
