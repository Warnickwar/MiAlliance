package com.mialliance.mind.base.communication;

import net.minecraft.world.entity.ai.memory.ExpirableValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class CommunicationTracker {

    private final Set<ExpirableValue<Communication>> recentCommunications;
    private final long defaultTimeToRemember;

    public CommunicationTracker(long defaultTimeToRemember) {
        this.recentCommunications = new HashSet<>();
        this.defaultTimeToRemember = defaultTimeToRemember;
    }

    public void tick() {
        recentCommunications.forEach(val -> {
            val.tick();
            if (val.hasExpired()) recentCommunications.remove(val);
        });
    }

    public void addCommunication(@NotNull Communication comm, long timeToRemember) {
        recentCommunications.add(ExpirableValue.of(comm, timeToRemember));
    }

    public void addCommunication(@NotNull Communication comm) {
        addCommunication(comm, defaultTimeToRemember);
    }

    public boolean remembersCommunication(@NotNull Communication comm) {
        for (ExpirableValue<Communication> commMemory : recentCommunications) {
            if (commMemory.getValue() == comm) return true;
        }
        return false;
    }
}
