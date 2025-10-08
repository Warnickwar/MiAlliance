package com.mialliance.components;

public interface ComponentObject {
    <O extends ComponentObject> ComponentManager<O> getManager();
}
