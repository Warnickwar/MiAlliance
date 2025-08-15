package com.mialliance.mind.base.communication;

import org.jetbrains.annotations.NotNull;

/**
 * An interface which indicates that an object is capable of accepting {@link Communication Communiations} from {@link CommDispatcher Communication Dispatchers}.
 */
public interface CommListener {

    /**
     * Handles any incoming {@link Communication Communications} from any {@link CommDispatcher Communication Dispatchers}.
     * Whether the listener accepts the Communication is up to implementation.
     * @param comm The incoming Communication reaching the Listener.
     */
    void onRecieveMessage(@NotNull Communication comm);
}
