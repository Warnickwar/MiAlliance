package com.mialliance.mind.implementation.kits;

public enum BehaviorSlot implements IBehaviorSlot {
    MAIN("main");

    private final String id;

    BehaviorSlot(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
