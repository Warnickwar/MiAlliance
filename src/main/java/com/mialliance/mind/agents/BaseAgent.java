package com.mialliance.mind.agents;

import com.mialliance.mind.events.EventManager;
import com.mialliance.mind.events.IEvent;
import com.mialliance.mind.events.IEventListener;
import com.mialliance.mind.memories.ImmutableMemoryManager;
import com.mialliance.mind.memories.MemoryManager;
import com.mialliance.mind.planning.TaskPlan;
import com.mialliance.mind.planning.TaskPlanner;
import com.mialliance.mind.sensors.BaseSensor;
import com.mialliance.mind.sensors.SensorKey;
import com.mialliance.mind.sensors.SensorManager;
import com.mialliance.mind.sensors.SensorSupplier;
import com.mialliance.mind.tasks.BaseTask;
import com.mialliance.mind.tasks.CompoundTask;
import com.mialliance.mind.tasks.TaskTraversal;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class BaseAgent<O extends MindOwner> {

    private final O owner;

    private final MemoryManager memories;
    private final SensorManager<O> sensors;
    private final EventManager events;

    private CompoundTask<O> domain;
    private TaskPlan<O> plan;

    public BaseAgent(@NotNull O owner, @NotNull CompoundTask<O> domain) {
        this.domain = domain;
        this.owner = owner;

        this.memories = new MemoryManager();
        this.sensors = new SensorManager<>();
        this.events = new EventManager();
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
        if (this.plan != null) this.plan.end();
    }

    // <---> BEHAVIOR TREE MANAGEMENT <--->

    public <T extends BaseTask<O>> boolean addTask(String path, T task) {
        Optional<BaseTask<O>> result = TaskTraversal.findChild(domain, path);
        if (result.isEmpty()) return false;

        BaseTask<O> taskResult = result.get();
        if (!(taskResult instanceof CompoundTask<O> cTask)) return false;

        cTask.addChild(task);
        return true;
    }

    public <T extends BaseTask<O>> boolean addTask(String path, int index, T task) {
        Optional<BaseTask<O>> result = TaskTraversal.findChild(domain, path);
        if (result.isEmpty()) return false;

        BaseTask<O> taskResult = result.get();
        if (!(taskResult instanceof CompoundTask<O> cTask)) return false;

        cTask.addChild(task, index);
        return true;
    }

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

        if (this.plan == null || this.plan.isComplete()) {
            this.plan = TaskPlanner.makePlan(this.owner);
        }

        this.sensors.tick();
        this.plan.tick();

        this.onTick();
    }

    protected boolean shouldTick() {
        return true;
    }

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

    // <---> SERIALIZATION <--->
    // TODO: Fucking do Serialization. Codecs are painful, and I'm tired.

}
