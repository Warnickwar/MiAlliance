package com.mialliance.utils;

import com.mialliance.entities.AbstractMi;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WorldUtils {

    @Nullable
    public static AbstractMi getMi(int entityID, Level level) {
        Entity foundEnt = level.getEntity(entityID);
        if (foundEnt == null) return null;
        return foundEnt instanceof AbstractMi mi ? mi : null;
    }

}
