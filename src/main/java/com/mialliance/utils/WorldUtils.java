package com.mialliance.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WorldUtils {

    // TODO: See how expensive this calculation is.
    @Nullable
    public static AbstractMi getMi(int entityID, Level level) {
        Entity foundEnt = level.getEntity(entityID);
        if (foundEnt == null) return null;
        return foundEnt instanceof AbstractMi mi ? mi : null;
    }

}
