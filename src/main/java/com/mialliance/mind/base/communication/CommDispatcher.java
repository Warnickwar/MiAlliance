package com.mialliance.mind.base.communication;

import java.util.Set;

/**
 * An interface which indicates that an object is capable of sending new {@link Communication Communications} to any {@link CommListener Listeners}.
 */
public interface CommDispatcher {

    /**
     * A function which returns a Set of unique {@link CommListener Listeners} which should accept the released {@link Communication Communication}.
     * @return A Set containing every {@link CommListener listener} that can accept a fired Communication.
     */
    Set<CommListener> getListeners();
}
