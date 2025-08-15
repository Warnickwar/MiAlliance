package com.mialliance.utils;

import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public interface OwnerTeamSupplier {
    @Nullable PlayerTeam getOwnerTeam();
}
