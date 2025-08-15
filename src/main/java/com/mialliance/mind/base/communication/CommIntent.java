package com.mialliance.mind.base.communication;

public interface CommIntent {

    /**
     * A function which indicates if an Intention should be considered Priority, or Important.
     * Priority Intentions tend to be considered less strictly when used, indicating imperitive State changes
     * to {@link CommListener Listeners}.
     * @return Whether the Communication Intention should be considered Priority.
     */
    boolean priorityIntent();
}
