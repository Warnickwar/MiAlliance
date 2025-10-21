package com.mialliance.mind.base.belief;

import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.memory.ImmutableMemoryManager;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p>
 *
 * </p>
 */
public final class BeliefFactory {

    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    private final MindAgent<?> agent;
    @NotNull
    private final HashMap<String, MindBelief> beliefs;

    /**
     * <p>
     *      Returns a new Factory which can be used to simplify the initial construction of
     *      {@link MindBelief Beliefs} for the associated Agent.
     * </p>
     * @param agent The Agent which is
     * @param beliefs
     */
    public BeliefFactory(@NotNull MindAgent<?> agent, @NotNull HashMap<String, MindBelief> beliefs) {
        this.agent = agent;
        this.beliefs = beliefs;
    }

    public void addBelief(@NotNull String key, @NotNull Supplier<Boolean> condition) {
        warnDuplicate(key);

        this.beliefs.put(key, new MindBelief.Builder(key)
            .withCondition(condition)
            .build());
    }

    public <T> void addMemoryBelief(@NotNull String key, @NotNull MemoryModuleType<T> type, @NotNull ImmutableMemoryManager memories, @NotNull Predicate<T> condition) {
        this.addBelief(key, () -> memoryMeetsCondition(memories, type, condition));
    }

    public void addMemoryPresenceBelief(@NotNull String key, @NotNull MemoryModuleType<?> type, @NotNull ImmutableMemoryManager memories, boolean shouldBePresent) {
        this.addBelief(key, () -> checkMemoryPresence(memories, type, shouldBePresent));
    }

    public void addLocationBelief(@NotNull String key, float distance, @NotNull Vec3 location) {
        warnDuplicate(key);

        this.beliefs.put(key, new MindBelief.Builder(key)
            .withCondition(() -> inRangeOf(agent.getLocation(), location, distance))
            .withLocation(() -> location)
            .build());
    }

    public void addLocationBelief(@NotNull String key, float distance, @NotNull Supplier<Vec3> location) {
        warnDuplicate(key);

        this.beliefs.put(key, new MindBelief.Builder(key)
            .withCondition(() -> inRangeOf(agent.getLocation(), location.get(), distance))
            .withLocation(location)
            .build());
    }

    public void addConditionalLocationBelief(@NotNull String key, Supplier<Boolean> conditional, Supplier<Vec3> location) {
        warnDuplicate(key);

        this.beliefs.put(key, new MindBelief.Builder(key)
            .withCondition(conditional)
            .withLocation(location)
            .build());
    }

    private boolean inRangeOf(Vec3 origin, Vec3 target, float range) { return origin.closerThan(target, range); }

    private void warnDuplicate(@NotNull String key) {
        if (this.beliefs.containsKey(key)) {
            LOGGER.error("Belief {} already exists; Is this a duplicate registration?  | {}", key, new IllegalStateException());
        }
    }

    private static boolean checkMemoryPresence(@NotNull ImmutableMemoryManager manager, @NotNull MemoryModuleType<?> type, boolean shouldBePresent) {
        return manager.hasMemory(type) == shouldBePresent;
    }

    private static <T> boolean memoryMeetsCondition(@NotNull ImmutableMemoryManager manager, @NotNull MemoryModuleType<T> type, @NotNull Predicate<T> condition) {
        T val = manager.getMemory(type);
        return val != null && condition.test(val);
    }

}
