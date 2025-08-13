package org.warnickwar.components.functional.definitions.components;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.warnickwar.components.functional.Component;
import org.warnickwar.components.functional.ComponentType;
import org.warnickwar.components.functional.definitions.handlers.EntityComponentHandler;

public class EntityTestComponent extends Component<EntityComponentHandler<?>> {

    private static final ParticleType<?> particleType = ParticleTypes.HAPPY_VILLAGER;
    private static final int MAX_DELAY = 10;

    private int currentDelay = 0;

    public EntityTestComponent(@NotNull ComponentType<?,EntityComponentHandler<?>> type, @NotNull EntityComponentHandler<?> handler) {
        super(type, handler);
    }

    @Override
    public void start() {

    }

    @Override
    public void tick() {
        if (currentDelay++ < MAX_DELAY) return;
        Entity ent = this.getHandler().getParent();
        ent.level().addParticle((ParticleOptions) particleType, ent.getX(), ent.getEyeY()+0.5f, ent.getZ(), 0, 5, 0);
    }

}
