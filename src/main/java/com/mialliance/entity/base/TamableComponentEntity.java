package com.mialliance.entity.base;

import com.mialliance.components.ComponentManager;
import com.mialliance.components.EntityComponentObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class TamableComponentEntity extends PathfinderMob implements OwnableEntity, EntityComponentObject<TamableComponentEntity> {

    private final ComponentManager componentManager;

    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamableComponentEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected TamableComponentEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        this.componentManager = new ComponentManager(this);
    }

    // Override later
    public void tame(Player player) {
        this.setOwnerUUID(player.getUUID());
    }

    protected void setupComponents(ComponentManager componentManager) {

    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID owner) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(owner));
    }

    @Override
    public @Nullable Entity getOwner() {
        try {
            UUID uuid = this.getOwnerUUID();
            return uuid == null ? null : this.level.getPlayerByUUID(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public TamableComponentEntity getEntity() {
        return this;
    }

    @Override
    public ComponentManager getManager() {
        return this.componentManager;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override
    public boolean save(CompoundTag tag) {
        //tag.put("components", this.componentManager.save());
        this.entityData.get(DATA_OWNERUUID_ID).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        return super.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
//        if (tag.contains("components")) {
//            this.componentManager.load(tag.getCompound("components"));
//        }
        UUID ownerUUID = null;
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }

        if (ownerUUID != null) {
            setOwnerUUID(uuid);
        }
        super.load(tag);
    }

}
