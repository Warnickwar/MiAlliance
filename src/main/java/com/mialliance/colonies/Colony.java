package com.mialliance.colonies;

import com.mialliance.communication.CommDispatcher;
import com.mialliance.communication.CommListener;
import com.mialliance.communication.Communication;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class Colony implements CommListener, CommDispatcher {

    @Override
    public Set<CommListener> getListeners() {
        return Set.of();
    }

    // TODO: New Messages should be saved to the Colony BlackBoard, and then handed off to Mis as they request information.
    //  Additionally, certain Memories should be used in order to influence the Colony to Attack, Defend, etc.
    @Override
    public void onRecieveMessage(@NotNull Communication comm) {}

}
