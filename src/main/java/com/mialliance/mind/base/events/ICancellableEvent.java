package com.mialliance.mind.base.events;

public interface ICancellableEvent {

    boolean isCancelled();
    void setCancelled(boolean result);

}
