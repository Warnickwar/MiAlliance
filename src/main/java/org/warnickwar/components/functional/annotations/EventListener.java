package org.warnickwar.components.functional.annotations;

import org.warnickwar.components.functional.events.ComponentEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An alternative method to register FunctionalComponent EventListeners.
 * This is far simpler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {
    Class<? extends ComponentEvent> value();
}