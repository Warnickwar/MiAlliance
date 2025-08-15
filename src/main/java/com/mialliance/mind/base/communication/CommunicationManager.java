package com.mialliance.mind.base.communication;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class CommunicationManager {

    /**
     * Emits a {@link Communication Communication} to {@link CommListener Listeners} that the {@link CommDispatcher Dispatcher} provides.
     * @param origin The original Dispatcher which is firing the Communication.
     * @param communication The Communication being relayed to Listeners.
     * @return True if there were listeners that could accept the communication, False if there were no listeners.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public static boolean emitCommunication(@NotNull CommDispatcher origin, @NotNull Communication communication) {
        Set<CommListener> listeners = origin.getListeners();
        listeners.remove(origin);

        if (listeners.isEmpty()) return false;

        listeners.forEach(list -> list.onRecieveMessage(communication));
        return true;
    }
}
