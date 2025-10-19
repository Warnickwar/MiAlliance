package com.mialliance.advancements;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

public class RecruitMiTrigger {

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite ent, ResourceLocation p_16975_, EntityPredicate.Composite p_16976_) {
            super(p_16975_, p_16976_);
            this.entity = ent;
        }

//        public static TriggerInstance recruitMi() {
//
//        }

    }
}
