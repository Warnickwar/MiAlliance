package org.warnickwar.components.registries.definitions;

import org.warnickwar.components.functional.ComponentHandler;
import org.warnickwar.components.functional.ComponentType;
import org.warnickwar.components.functional.definitions.components.EmptyComponent;
import org.warnickwar.components.functional.definitions.components.EntityTestComponent;
import org.warnickwar.components.functional.definitions.handlers.EntityComponentHandler;

public class ModComponents {


    public static final ComponentType<EntityTestComponent, EntityComponentHandler<?>> ENTITY_TESTABLE = ComponentType.of(EntityTestComponent::new);

    // Default value
    public static final ComponentType<EmptyComponent, ComponentHandler<?>> EMPTY = ComponentType.of(EmptyComponent::new);

}
