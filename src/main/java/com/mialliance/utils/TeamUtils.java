package com.mialliance.utils;

import com.mialliance.entities.AbstractMi;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public final class TeamUtils {

    public static boolean ownerOnSameTeam(@NotNull AbstractMi one, @NotNull AbstractMi two) {
        return one.getOwnerTeam() == two.getOwnerTeam();
    }

    public static boolean ownerOnSameTeam(@NotNull AbstractMi one, @NotNull Player two) {
        return one.getOwnerTeam() == two.getTeam();
    }
}
