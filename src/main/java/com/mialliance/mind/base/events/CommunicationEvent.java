package com.mialliance.mind.base.events;

import com.mialliance.communication.Communication;
import org.jetbrains.annotations.NotNull;

public class CommunicationEvent implements IEvent, ICancellableEvent {

    private final Communication comm;
    private boolean isCancelled;

    public CommunicationEvent(@NotNull Communication comm) {
        this.comm = comm;
        this.isCancelled = false;
    }

    @NotNull
    public Communication getComm() {
        return this.comm;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean result) {
        this.isCancelled = result;
    }

}
