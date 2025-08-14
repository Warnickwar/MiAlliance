package com.mialliance.entities;

import com.mialliance.mind.base.agents.BaseAgent;
import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.tasks.CompoundTask;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;

// TODO: Functional Component and Renderable Component system.
public abstract class AbstractMi<O extends AbstractMi<?>> extends TamableAnimal implements EntityMindOwner<O>, Enemy {

    protected final BaseAgent<O> agentController;

    @SuppressWarnings("unchecked")
    protected AbstractMi(EntityType<? extends TamableAnimal> p_21803_, Level p_21804_) {
        super(p_21803_, p_21804_);
        this.agentController = new BaseAgent<>((O) this, this.generateDomainBehaviors());
    }

    protected abstract CompoundTask<O> generateDomainBehaviors();

    @SuppressWarnings("unchecked")
    @Override
    public BaseAgent<O> getAgent() {
        return this.agentController;
    }

    @Override
    public void tick() {
        super.tick();
        this.level.getProfiler().push("mialliance:tickingBehaviorAgent");
        agentController.tick();
        this.level.getProfiler().pop();
    }

    public boolean isEntityTarget(LivingEntity ent) {
        if (ent instanceof TamableAnimal animal) {
            // Don't target animals which are tamed to the Leader
            if (animal.getOwnerUUID() == this.getOwnerUUID()) {
                return false;
            } else if (animal instanceof AbstractMi<?> mi) {
                // Don't target allied Mis
                return mi.getTeam() != this.getTeam() && !ownerOnSameTeam(this, mi);
            }
        }
        return true;
    }

    public static boolean ownerOnSameTeam(AbstractMi<?> one, AbstractMi<?> two) {
        Scoreboard scoreboard;
        if (one.getOwnerUUID() == null && one.getOwnerUUID() != two.getOwnerUUID()) return false; // Should always return false if both have no owner, aka Alliance Mi
        if (two.getOwnerUUID() == null) return false; // A final check necessary just in case it didn't go through the first time
        return (scoreboard = one.getLevel().getScoreboard()).getPlayersTeam(one.getOwnerUUID().toString()) == scoreboard.getPlayersTeam(two.getOwnerUUID().toString());
    }

    @Override
    public boolean save(@NotNull CompoundTag tag) {
        tag.put("agentInformation", this.agentController.save(new CompoundTag()));
        return super.save(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        this.agentController.load(tag.getCompound("agentInformation"));
        super.load(tag);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final O getOwner() {
        return (O) this;
    }

}
