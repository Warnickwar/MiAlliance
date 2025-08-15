package com.mialliance.entities;

import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.communication.CommListener;
import com.mialliance.mind.base.tasks.CompoundTask;
import com.mialliance.mind.implementations.agents.MiAgent;
import com.mialliance.utils.OwnerTeamSupplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: Functional Component and Renderable Component system.
public abstract class AbstractMi extends TamableAnimal implements Enemy, EntityMindOwner<AbstractMi>, CommListener, OwnerTeamSupplier {

    private final MiAgent agentInstance;

    protected AbstractMi(EntityType<? extends TamableAnimal> p_21803_, Level p_21804_) {
        super(p_21803_, p_21804_);
        this.agentInstance = this.createAgent(this.createBehaviorDomain());
    }

    // protected abstract CompoundTask<O> generateDomainBehaviors();

    @Override
    public void tick() {
        super.tick();
        this.level.getProfiler().push("mialliance:tickingBehaviorAgent");
        // Agent Instance handles ticking only if the Entity can tick.
        agentInstance.tick();
        this.level.getProfiler().pop();
    }

    /**
     * An Entity predicate indicating whether the current Entity should be attacked.
     * @param ent The LivingEntity that is being tested.
     * @return Whether the LivingEntity is a valid target.
     */
    public boolean isEntityTarget(LivingEntity ent) {
        if (ent instanceof TamableAnimal animal) {
            // Don't target animals which are tamed to the Leader
            if (animal.getOwnerUUID() == this.getOwnerUUID()) {
                return false;
            } else if (animal instanceof AbstractMi mi) {
                // Don't target allied Mis
                return mi.getTeam() != this.getTeam() && !ownerOnSameTeam(this, mi);
            }
        }
        return true;
    }

    // Suppression is okay because, at minimum, Mis' Behavior Agents will always inherit
    //  MiAgent.
    @SuppressWarnings("unchecked")
    public MiAgent getAgent() {
        return agentInstance;
    }

    @Override
    public @Nullable PlayerTeam getOwnerTeam() {
        if (this.getOwnerUUID() == null) return null;
        // TODO: Custom Team implementation, because Players not on a team will return Null.
        return this.getLevel().getScoreboard().getPlayerTeam(this.getOwnerUUID().toString());
    }

    @Override
    public @NotNull AbstractMi getEntity() {
        return this;
    }

    /**
     * A constructor function which creates and returns the Mi's Behavior Agent.
     * The Agent must inherit or be a MiAgent, at minimum.
     * @param domain The Behavior Tree made for the Agent. This must be passed to the Agent upon construction.
     * @return A new Agent instance for the Mi.
     */
    protected abstract MiAgent createAgent(CompoundTask<EntityMindOwner<AbstractMi>> domain);

    /**
     * Creates the default Behavior Domain for the current Mi Agent upon initialization.
     * @return The constructed Domain which encompasses all default behaviors of the Mi for the Agent.
     */
    protected abstract CompoundTask<EntityMindOwner<AbstractMi>> createBehaviorDomain();

    public static boolean ownerOnSameTeam(AbstractMi one, AbstractMi two) {
        return one.getOwnerTeam() == two.getOwnerTeam();
    }

}
