package com.mialliance.components.implementations;

import com.mialliance.components.Component;
import com.mialliance.components.ComponentObject;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.planning.TaskPlan;
import com.mialliance.mind.base.tasks.CompoundTask;
import com.mialliance.registers.ModComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BehaviorTreeComponent<O extends ComponentObject, M extends MindOwner> extends Component<O> {

    @Nullable
    private CompoundTask<M> domain = null;
    @Nullable
    private TaskPlan<M> currentPlan;

    @Nullable
    private MemoryComponent memories = null;

    public void setDomain(@NotNull CompoundTask<M> domain) {
        this.domain = domain;
    }

    @Override
    protected void onEnable() {
        this.memories = this.getComponent(ModComponents.GENERIC.MEMORY_COMPONENT);
    }

    @Override
    protected void onDisable() {
        this.memories = null;
    }

}
