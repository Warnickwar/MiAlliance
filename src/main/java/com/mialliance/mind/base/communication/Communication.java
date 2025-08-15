package com.mialliance.mind.base.communication;

import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.memories.TemplateValue;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class Communication {

    private final @NotNull CommDispatcher origin;
    private final @NotNull CommIntent intent;
    private final @NotNull Set<TemplateValue<?>> effects;

    public Communication(@NotNull CommDispatcher origin, @NotNull CommIntent intent, @NotNull Set<TemplateValue<?>> effects) {
        this.origin = origin;
        this.intent = intent;
        this.effects = effects;
    }

    @NotNull
    public CommDispatcher getOrigin() {
        return this.origin;
    }

    @NotNull
    public CommIntent getIntent() {
        return this.intent;
    }

    public void applyToMemories(@NotNull MemoryManager manager) {
        effects.forEach(val -> val.applyToMemories(manager));
    }

}
