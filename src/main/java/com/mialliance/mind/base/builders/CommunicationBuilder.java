package com.mialliance.mind.base.builders;

import com.mialliance.communication.CommDispatcher;
import com.mialliance.communication.CommIntent;
import com.mialliance.communication.Communication;
import com.mialliance.communication.CommunicationManager;
import com.mialliance.mind.base.memories.TemplateValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class CommunicationBuilder {

    private final @NotNull CommDispatcher origin;
    private final @NotNull CommIntent intention;
    private final @NotNull Set<TemplateValue<?>> effects;

    private CommunicationBuilder(@NotNull CommDispatcher origin, @NotNull CommIntent intent) {
        this.origin = origin;
        this.intention = intent;
        this.effects = new HashSet<>();
    }

    @NotNull
    public <T> CommunicationBuilder addEffect(TemplateValue<T> value) {
        effects.add(value);
        return this;
    }

    @NotNull
    public <T> CommunicationBuilder addEffect(MemoryModuleType<T> key, T value) {
        return this.addEffect(key, value, Long.MAX_VALUE);
    }

    @NotNull
    public <T> CommunicationBuilder addEffect(MemoryModuleType<T> key, T value, long expiry) {
        return addEffect(TemplateValue.addExpirableMemory(key, value, expiry));
    }

    public Communication build() {
        return new Communication(this.origin, this.intention, this.effects);
    }

    public void emit() {
        CommunicationManager.emitCommunication(this.origin, this.build());
    }

    public static CommunicationBuilder from(@NotNull CommDispatcher origin, @NotNull CommIntent intention) {
        return new CommunicationBuilder(origin, intention);
    }
}
