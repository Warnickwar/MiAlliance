package org.warnickwar.components.functional.events;

import org.warnickwar.components.functional.ComponentType;

public class ComponentRemovedEvent extends ComponentEvent {

    private final ComponentType<?,?> type;

    public ComponentRemovedEvent(ComponentType<?,?> type) {
        this.type = type;
    }

    public ComponentType<?,?> getType() {
        return type;
    }
}
