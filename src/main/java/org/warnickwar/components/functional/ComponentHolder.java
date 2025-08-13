package org.warnickwar.components.functional;

// Indicates that the Object holds a GenericComponentHandler of a specific type
public interface ComponentHolder<H extends ComponentHandler<?>> {
    H getComponentHandler();
}
