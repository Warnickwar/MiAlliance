package com.mialliance.items;

import com.mialliance.mind.base.agent.EntityMindAgentHolder;
import com.mialliance.mind.base.kits.Behavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BehaviorTestItem extends Item {

    private final Behavior behavior;

    public BehaviorTestItem(Properties p_41383_, Behavior behavior) {
        super(p_41383_);
        this.behavior = behavior;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (target instanceof EntityMindAgentHolder holder) {
            if (!behavior.canApplyTo(holder.getAgent())) return InteractionResult.FAIL;
            if (player.isCrouching()) {
                behavior.removeFromAgent(holder.getAgent());
            } else {
                behavior.applyToAgent(holder.getAgent());
            }
            holder.getAgent().finishPlan();
            return InteractionResult.SUCCESS;
        }


        return InteractionResult.FAIL;
    }

}
