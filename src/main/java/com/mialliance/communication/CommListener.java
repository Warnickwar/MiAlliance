package com.mialliance.communication;

import org.jetbrains.annotations.NotNull;

/**
 * An interface which indicates that an object is capable of accepting {@link Communication Communiations} from {@link CommDispatcher Communication Dispatchers}.
 * This interface is part of the wider {@code Communication System}.
 */
public interface CommListener {

    /**
     * <p>
     *      Handles any incoming {@link Communication Communications} from any {@link CommDispatcher Communication Dispatchers}.
     *      Whether the listener accepts the Communication is up to implementation.
     * </p>
     * <p>
     *     A key attribute of the {@code Communication System} is that it is run asynchronously from the primary server thread.
     *     As such, any functions which accepts- or should accept- a {@link Communication Communication} should be prepared to handle the incoming
     *     {@link Communication Communication} away from the primary server thread. Care must be taken to avoid crashing.
     * </p>
     * @param comm The incoming Communication reaching the Listener.
     */
    void onRecieveMessage(@NotNull Communication comm);
}
