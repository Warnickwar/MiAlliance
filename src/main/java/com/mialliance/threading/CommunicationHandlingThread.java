package com.mialliance.threading;

import com.mialliance.mind.base.communication.CommListener;
import com.mialliance.mind.base.communication.Communication;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Queue;
import java.util.Set;

/**
 * A Thread implementation handling the dispersal and execution of Communications
 *  throughout the server.
 */
public class CommunicationHandlingThread extends Thread {

    // Avoid using the Manager's logger, to avoid Synchronization errors
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Queue<Pair<Set<CommListener>, Communication>> communicationsToManage;
    private volatile boolean run = false;

    public CommunicationHandlingThread(Queue<Pair<Set<CommListener>, Communication>> communicationSet) {
        this.communicationsToManage = communicationSet;
    }

    @Override
    public void run() {
        while (run) {
            // Only check if it is empty while having the lock;
            //  Avoid extensive handling while synchronized
            synchronized (this.communicationsToManage) {
                if (communicationsToManage.isEmpty()) {
                    try {
                        // Release our lock on the Queue, and
                        //  pause the thread on the Queue so resources aren't wasted on an empty queue
                        communicationsToManage.wait();
                    } catch (InterruptedException e) {
                        // Somehow already interrupted?
                        //  Reinterrupt to avoid issues-we're supposed to be paused anyway!
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            Pair<Set<CommListener>, Communication> nextComm;
            // Only synchronize for when we actually need the
            //  Queue, so that the Server Thread is not blocked
            //  And we can accept the next group.
            synchronized (this.communicationsToManage) {
                // Take and remove the next object in Queue
                nextComm = this.communicationsToManage.poll();
            }

            // How would this ever happen?
            if (nextComm == null) continue;

            // Dispatch the Communication to all Agents, let them handle it.
            //  It is imperative that Agents responsibly handle the Communication
            Communication comm = nextComm.getSecond();
            nextComm.getFirst().forEach(listener -> {
                try {
                    listener.onRecieveMessage(comm);
                } catch (Exception e) {
                    LOGGER.error("A Communication Listener could not handle Communication {}!", comm, e);
                }
            });
        }
    }

    public void stopHandling() {
        this.run = false;
    }

    @Override
    public synchronized void start() {
        this.run = true;
        super.start();
    }

}
