package com.mialliance.communication;

import com.mialliance.mind.base.memory.MemoryManager;
import com.mialliance.mind.base.memory.TemplateValue;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record Communication(@NotNull CommDispatcher origin, @NotNull CommIntent intent,
                            @NotNull Set<TemplateValue<?>> effects) {

    public void applyToMemories(@NotNull MemoryManager manager) {
        effects.forEach(val -> val.applyToMemories(manager));
    }

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder("Communication{intent=").append(intent).append(",effects=[");
        effects.forEach(builder::append);
        return builder.append("]}").toString();
    }
}
