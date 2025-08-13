package org.warnickwar.components.functional.definitions.components;

import org.jetbrains.annotations.NotNull;
import org.warnickwar.components.functional.Component;
import org.warnickwar.components.functional.ComponentHandler;
import org.warnickwar.components.functional.ComponentType;
import org.warnickwar.components.utils.DebugUtils;

public final class EmptyComponent extends Component<ComponentHandler<?>> {

    public EmptyComponent(@NotNull ComponentType<?, ComponentHandler<?>> type, @NotNull ComponentHandler<?> handler) {
        super(type, handler);
    }

    @Override
    public void start() {
        DebugUtils.logIfDebug("Adding an Empty Component on a ComponentHandler; Maybe something went wrong?");
    }

    @Override
    public void tick() {}

    @Override
    public void removed() {
        DebugUtils.logIfDebug("Removing an EmptyComponent on a ComponentHandler; Maybe something went wrong?");
    }

}
