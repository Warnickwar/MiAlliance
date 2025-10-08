package com.mialliance.mind.base.agents;

import com.mialliance.communication.CommDispatcher;
import com.mialliance.communication.CommListener;
import com.mialliance.mind.base.events.EventManager;
import com.mialliance.mind.base.events.IEvent;
import com.mialliance.mind.base.events.IEventListener;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.planning.TaskPlan;
import com.mialliance.mind.base.planning.TaskPlanner;
import com.mialliance.mind.base.sensors.BaseSensor;
import com.mialliance.mind.base.sensors.SensorKey;
import com.mialliance.mind.base.sensors.SensorManager;
import com.mialliance.mind.base.sensors.SensorSupplier;
import com.mialliance.mind.base.tasks.BaseTask;
import com.mialliance.mind.base.tasks.CompoundTask;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class BaseAgent<O extends MindOwner> implements CommListener, CommDispatcher {

    private static final String MEMORIES_DATA_KEY = "memories";

    protected final O owner;

    private MemoryManager memories;
    private final SensorManager<O> sensors;
    private final EventManager events;

    private CompoundTask<O> domain;
    private final AtomicReference<TaskPlan<O>> plan;
    @Nullable
    private volatile Future<TaskPlan<O>> thinkingPlan = null;

    public BaseAgent(@NotNull O owner, @NotNull CompoundTask<O> domain) {
        this.domain = domain;
        this.owner = owner;

        this.memories = new MemoryManager();
        this.sensors = new SensorManager<>();
        this.events = new EventManager();

        this.plan = new AtomicReference<>(null);
    }

    // <---> Getters / Setters <--->

    public O getOwner() {
        return this.owner;
    }

    public final MemoryManager getMemories() {
        return this.memories;
    }

    public CompoundTask<O> getDomain() {
        return this.domain;
    }

    public void setDomain(CompoundTask<O> domain) {
        this.domain = domain;
        this.abortCurrentPlan();
    }

    public final void abortCurrentPlan() {
        this.plan.setRelease(null);
    }

    // <---> BEHAVIOR TREE MANAGEMENT <--->

    /**
     * Attempts to add a new Behavior Task at the indicated location. Used for modular changes.
     * The Task will only be added if the path leads to a CompoundTask, and is not null.
     * @param path The stringified path to traverse to.
     * @param task The task that is attempting to be added.
     * @return whether the function was successful in adding the Task.
     * @param <T> The type of task being added.
     */
    public <T extends BaseTask<O>> boolean addTask(String path, T task) {
        Optional<BaseTask<O>> result = TaskTraversal.findChild(domain, path);
        if (result.isEmpty()) return false;

        BaseTask<O> taskResult = result.get();
        if (!(taskResult instanceof CompoundTask<O> cTask)) return false;

        cTask.addChild(task);
        return true;
    }

    /**
     * Attempts to add a new Behavior Task at the indicated location. Used for modular changes.
     * The Task will only be added if the path leads to a CompoundTask, and is not null.
     * @param path The stringified path to traverse to.
     * @param index The integer <b>Priority</b> of the resulting task. Lower values indicate a higher priority.
     * @param task The task that is attempting to be added.
     * @return whether the function was successful in adding the Task.
     * @param <T> The type of task being added.
     */
    public <T extends BaseTask<O>> boolean addTask(String path, int index, T task) {
        Optional<BaseTask<O>> result = TaskTraversal.findChild(domain, path);
        if (result.isEmpty()) return false;

        BaseTask<O> taskResult = result.get();
        if (!(taskResult instanceof CompoundTask<O> cTask)) return false;

        cTask.addChild(task, index);
        return true;
    }

    /**
     * Attempts to remove an existing Task from the Behavior Tree. This function should be used with caution,
     * as removing necessary Tasks from the tree could lobotomize the Agent, and fail to properly work.
     * @param path The path to the task to remove.
     * @return An Optional either containing the removed task, or an empty Optional upon failing to remove the task.
     */
    public Optional<BaseTask<O>> removeTask(String path) {
        String assetID = path.substring(path.lastIndexOf(':')+1);
        String pathID = path.substring(0, path.lastIndexOf(':'));
        Optional<BaseTask<O>> result = TaskTraversal.findChild(domain, pathID);
        if (result.isEmpty()) return Optional.empty();

        BaseTask<O> taskResult = result.get();
        if (!(taskResult instanceof CompoundTask<O> cTask)) {
            return Optional.empty();
        }

        return cTask.removeChild(assetID);
    }

    // <---> MEMORIES <--->

    public final <T> void addMemory(MemoryModuleType<T> type, T value) {
        memories.setMemory(type, value);
    }

    public final <T> void addMemory(MemoryModuleType<T> type, T value, long expirationTime) {
        memories.setMemory(type, value, expirationTime);
    }

    public final <T> void addMemory(MemoryModuleType<T> type, Supplier<T> valueSupplier) {
        this.addMemory(type, valueSupplier.get());
    }

    public final <T> void addMemory(MemoryModuleType<T> type, Supplier<T> valueSupplier, long expirationTime) {
        this.addMemory(type, valueSupplier.get(), expirationTime);
    }

    public final <T> boolean compareMemory(MemoryModuleType<T> type, @Nullable T expected) {
        T val = memories.getMemory(type);
        return Objects.equals(val, expected);
    }

    public final <T> boolean compareMemory(MemoryModuleType<T> type, @Nullable T expected, boolean defaultVal) {
        return this.compareMemory(type,expected) || defaultVal;
    }

    public final <T> @Nullable T forgetMemory(MemoryModuleType<T> type) {
        return this.memories.removeMemory(type);
    }

    // <---> TICKING <--->

    public final void tick() {
        if (!this.shouldTick()) return;

        // Tick Sensors before the Plan is made, such that any new plans are properly evaluated and updated with proper memories
        //  This is less important now that planning is asynchronous, but it will allow the first evaluation to have updated information.
        //  Planning should not take more than a tick for any reason.
        this.sensors.tick();


        // NOTE: This comes with the caveat that whenever planning a new action plan, it takes 1 tick at minimum to evaluate.
        //  I suppose this is adequate enough for the sake of avoiding lag when many agents plan at the same time.
        if (this.thinkingPlan == null && (this.plan.get() == null || this.plan.get().isComplete())) {
            if (this.plan.get() != null) this.plan.set(null);
            this.thinkingPlan = TaskPlanner.makePlan(owner);
        } else if (this.thinkingPlan != null && Objects.requireNonNull(this.thinkingPlan).isDone()) {
            try {
                this.plan.set(Objects.requireNonNull(this.thinkingPlan).get());
                this.thinkingPlan = null;
            } catch (InterruptedException | ExecutionException e) {
                // TODO: More comprehensive logging with this.
                throw new IllegalStateException("An agent failed to update its plan!");
            }
        }

        if (this.plan.get() != null) {
            this.plan.get().tick();
        }

        this.onTick();
    }

    /**
     * Whether the current Agent should tick and update itself.
     * @return The agent's capability to tick
     */
    protected boolean shouldTick() {
        return true;
    }

    /**
     * Extra functions that should happen whenever a Tick happens.
     */
    protected void onTick() { }

    // <---> SENSORS <--->

    public <S extends BaseSensor<O>> void registerSensor(SensorKey<O, S> key, SensorSupplier<O, S> supplier) {
        this.sensors.registerSensor(key, supplier.create(this.owner));
    }

    public  void removeSensor(SensorKey<O, ?> key) {
        this.sensors.unregisterSensor(key);
    }

    // <---> EVENTS <--->

    public <E extends IEvent> boolean registerListener(Class<E> eventType, IEventListener<E> listener) {
        return this.events.registerListener(eventType, listener);
    }

    public <E extends IEvent> boolean removeListener(Class<E> eventType, IEventListener<E> listener) {
        return this.events.unregisterListener(eventType, listener);
    }

    public boolean removeListener(IEventListener<?> listener) {
        return this.events.unregisterListener(listener);
    }

    public <T extends IEvent> void emit(T event) {
        this.events.call(event);
    }

    // <---> SERIALIZATION <--->

    // TODO: Do better.

    public CompoundTag save(CompoundTag tag) {
        DataResult<Tag> memoriesSerialization = MemoryManager.CODEC.encodeStart(NbtOps.INSTANCE, this.memories);
        AtomicReference<Tag> finalTag = new AtomicReference<>(null);
        memoriesSerialization.get().ifLeft(finalTag::set);
        if (finalTag.get() != null) {
            tag.put(MEMORIES_DATA_KEY, finalTag.get());
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains(MEMORIES_DATA_KEY)) {
            Tag memories = tag.get(MEMORIES_DATA_KEY);
            Either<Pair<MemoryManager, Tag>, DataResult.PartialResult<Pair<MemoryManager, Tag>>> res = MemoryManager.CODEC.decode(NbtOps.INSTANCE, memories).get();
            res.ifLeft(pair -> this.memories = MemoryManager.of(pair.getFirst()));
        }
    }

}
