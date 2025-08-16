package com.mialliance.mind.base.communication;

import com.mialliance.threading.CommunicationHandlingThread;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class CommunicationManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Queue<Pair<Set<CommListener>, Communication>> queuedCommunications = new ConcurrentLinkedQueue<>();
    private static final CommunicationHandlingThread communicationThread = new CommunicationHandlingThread(queuedCommunications);

    static {
        communicationThread.setName("Communication Handling");
    }

    /**
     * <p>
     *     Queues up a {@link Communication Communication} for the {@code Communication Handling Thread} to handle.
     *     Keep in mind that the {@code Communication System} is <b>asynchronous</b>, so care must be taken when
     *     accepting Communications. Communications are accepted on a first-come first-serve basis.
     * </p>
     * @param origin The original Dispatcher which is firing the Communication.
     * @param communication The Communication being relayed to Listeners.
     * @return {@code True} if there were listeners that can accept the Communication, {@code False} if there were no listeners, or the Communications System is downed.
     * @see Communication Communication
     * @see CommDispatcher Communication Dispatcher
     * @see CommListener Communication Listener
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public static boolean emitCommunication(@NotNull CommDispatcher origin, @NotNull Communication communication) {
        if (!communicationThread.isAlive()) {
            LOGGER.error("Attempting to send Communication through the Communication Handler while the thread is closed! {}", communication);
            return false;
        }
        Set<CommListener> listeners = origin.getListeners();
        listeners.remove(origin);

        if (listeners.isEmpty()) return false;

        synchronized (queuedCommunications) {
            queuedCommunications.add(Pair.of(listeners, communication));
        }
        // We should only ever have 1 thread waiting at maximum on the Queue!
        queuedCommunications.notify();
        return true;
    }

    public static void openCommunications() {
        try {
            communicationThread.start();
            LOGGER.info("Communication Handling is open- Communications are now accepted!");
        } catch (IllegalThreadStateException ex) {
            // We catch an error instead of
            LOGGER.error("Communications Thread is active already! Was it forgotten to close from the last instance?", ex);
        }
    }

    public static void closeCommunications() {
        // Only stop the thread if it is alive-avoid erroring!
        if (communicationThread.isAlive()) {
            communicationThread.stopHandling();
        }
    }
}
