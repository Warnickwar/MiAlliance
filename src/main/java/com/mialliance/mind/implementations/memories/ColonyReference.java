package com.mialliance.mind.implementations.memories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ColonyReference {

    public static final Codec<ColonyReference> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        UUIDUtil.CODEC.fieldOf("uuid").forGetter(ColonyReference::getUUID),
        BlockPos.CODEC.fieldOf("location").forGetter(ColonyReference::getLoc)
    ).apply(inst, ColonyReference::new));

    private final UUID uuid;
    private final BlockPos loc;

    public ColonyReference(@NotNull UUID uuid, @NotNull BlockPos blockPos) {
        this.uuid = uuid;
        this.loc = blockPos;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public BlockPos getLoc() {
        return this.loc;
    }

}
