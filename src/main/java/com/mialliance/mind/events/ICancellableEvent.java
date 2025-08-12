package com.mialliance.mind.events;

public interface ICancellableEvent {

    boolean isCancelled();
    void setCancelled(Boolean result);

}
