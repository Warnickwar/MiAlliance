package com.mialliance.mind.implementations.communication;

import com.mialliance.mind.base.communication.CommDispatcher;
import com.mialliance.mind.base.communication.CommListener;
import com.mialliance.utils.OwnerTeamSupplier;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class CommunicationProxy implements CommDispatcher, OwnerTeamSupplier {

    private final Set<CommListener> listeners;
    @Nullable
    private final PlayerTeam team;

    public CommunicationProxy(@NotNull Set<CommListener> listeners, @Nullable PlayerTeam team) {
        this.listeners = listeners;
        this.team = team;
    }

    @Override
    public Set<CommListener> getListeners() {
        return Set.of();
    }

    @Override
    public @Nullable PlayerTeam getOwnerTeam() {
        return null;
    }

}
