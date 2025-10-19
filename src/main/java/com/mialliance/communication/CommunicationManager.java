package com.mialliance.communication;

import com.mialliance.threading.JobManager;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class CommunicationManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final HashMap<Level, List<CommListener>> registeredListeners = new HashMap<>();

    /**
     * <p>
     *     Queues up a {@link Communication Communication} for the {@code Communication Handling Thread} to handle.
     *     Keep in mind that the {@code Communication System} is <b>asynchronous</b>, so care must be taken when
     *     accepting Communications. Communications are accepted on a first-come first-serve basis.
     * </p>
     * @param origin The original Dispatcher which is firing the Communication.
     * @param communication The Communication being relayed to Listeners.
     * @return {@code True} if there were listeners that can accept the Communication, {@code False} if there were no listeners, or the Communications System is down.
     * @see Communication Communication
     * @see CommDispatcher Communication Dispatcher
     * @see CommListener Communication Listener
     */
    @SuppressWarnings({"SuspiciousMethodCalls", "UnusedReturnValue"})
    public static boolean emitCommunication(@NotNull CommDispatcher origin, @NotNull Communication communication) {
        if (!JobManager.isAlive()) {
            LOGGER.error("Attempting to send Communication through the Communication Handler while the thread is closed! {}", communication);
            return false;
        }
        Set<CommListener> listeners = origin.getListeners();
        listeners.remove(origin);

        if (listeners.isEmpty()) return false;

        // Redundant, but done in case of asynchronous closing of the server
        if (JobManager.isAlive()) {
            JobManager.submitJob(() -> listeners.forEach(listener -> listener.onRecieveMessage(communication)));
            return true;
        }
        return false;
    }

    public static void registerListener(@NotNull CommListener listener) {}
}
