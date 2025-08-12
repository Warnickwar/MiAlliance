package com.mialliance.mind.builders;

import com.mialliance.mind.memories.MemoryValue;
import com.mialliance.mind.memories.TemplateValue;
import com.mialliance.mind.tasks.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.*;
import java.util.function.Predicate;

public abstract class BehaviorTreeBuilder<O extends TaskOwner, T extends BaseTask<O>> {

    protected final String identifier;
    protected final HashMap<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions;

    BehaviorTreeBuilder(String identifier) {
        this.identifier = identifier;
        this.preconditions = new HashMap<>();
    }

    public static <O extends TaskOwner> RootBuilder<O> start(String identifier) {
        return new RootBuilder<>(identifier);
    }

    protected static <O extends TaskOwner> void addPrecondition(BehaviorTreeBuilder<O, ?> builder, MemoryModuleType<?> type, Predicate<MemoryValue<?>> valueCheck) {
        builder.preconditions.put(type, valueCheck);
    }

    abstract T build();

    public static class RootBuilder<O extends TaskOwner> extends CompoundBuilder<O> {

        RootBuilder(String identifier) {
            super(identifier);
        }

        @Override
        public RootBuilder<O> setState(CompoundState state) {
            return (RootBuilder<O>) super.setState(state);
        }

        @Override
        public RootBuilder<O> addChild(BehaviorTreeBuilder<O, ?> builder) {
            return (RootBuilder<O>) super.addChild(builder);
        }

        @Override
        public <T extends BaseTask<O>> RootBuilder<O> addChild(String identifier, CustomBuilder.TaskFactory<O,T> supplier, Map<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions) {
            return (RootBuilder<O>) super.addChild(identifier, supplier, preconditions);
        }

        @Override
        public <T extends BaseTask<O>> RootBuilder<O> addChild(String identifier, CustomBuilder.TaskFactory<O,T> supplier) {
            return (RootBuilder<O>) super.addChild(identifier, supplier);
        }

        public CompoundTask<O> construct() {
            return this.build();
        }

    }

    public static class CompoundBuilder<O extends TaskOwner> extends BehaviorTreeBuilder<O, CompoundTask<O>> {
        protected CompoundState<O> state;

        // Used solely in order to retain the children ordering-important in AI evaluation
        protected LinkedList<BehaviorTreeBuilder<O, ?>> children;

        @SuppressWarnings("unchecked")
        public CompoundBuilder(String identifier) {
            super(identifier);
            this.state = (CompoundState<O>) CompoundState.FALLBACK;
            this.children = new LinkedList<>();
        }

        public CompoundBuilder<O> setState(CompoundState<O> state) {
            this.state = state;
            return this;
        }

        public <T extends BaseTask<O>> CompoundBuilder<O> addChild(String identifier, CustomBuilder.TaskFactory<O,T> supplier, Map<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions) {
            return this.addChild(new CustomBuilder<>(identifier, supplier, preconditions));
        }

        public <T extends BaseTask<O>> CompoundBuilder<O> addChild(String identifier, CustomBuilder.TaskFactory<O,T> supplier) {
            return this.addChild(identifier, supplier, Map.of());
        }

        public CompoundBuilder<O> addChild(BehaviorTreeBuilder<O, ?> builder) {
            this.children.add(builder);
            return this;
        }

        @Override
        CompoundTask<O> build() {
            return new CompoundTask<>(identifier, state, preconditions, new LinkedList<>(children.stream().map(BehaviorTreeBuilder::build).toList()));
        }

    }

    // I highly recommend you just make your own Primitive classes.
    public static class PrimitiveBuilder<O extends TaskOwner> extends BehaviorTreeBuilder<O, GenericPrimitiveTask<O>> {

        private GenericPrimitiveTask.PrimitiveStart<O> onStart;
        private GenericPrimitiveTask.PrimitiveTick<O> onTick;
        private GenericPrimitiveTask.PrimitiveEnd<O> onEnd;

        Map<MemoryModuleType<?>, TemplateValue<?>> effects;

        @SuppressWarnings("unchecked")
        public PrimitiveBuilder(String identifier) {
            super(identifier);
            this.onStart = (GenericPrimitiveTask.PrimitiveStart<O>) GenericPrimitiveTask.PrimitiveStart.EMPTY;
            this.onTick = (GenericPrimitiveTask.PrimitiveTick<O>) GenericPrimitiveTask.PrimitiveTick.EMPTY;
            this.onEnd = (GenericPrimitiveTask.PrimitiveEnd<O>) GenericPrimitiveTask.PrimitiveEnd.EMPTY;
            effects = new HashMap<>();
        }

        public <T> PrimitiveBuilder<O> addEffect(MemoryModuleType<T> type, T value) {
            effects.put(type, new TemplateValue<>(type, value));
            return this;
        }

        public <T> PrimitiveBuilder<O> addEffect(MemoryModuleType<T> type, T value, long expiry) {
            effects.put(type, new TemplateValue<>(type, value, expiry));
            return this;
        }

        public PrimitiveBuilder<O> setOnStart(GenericPrimitiveTask.PrimitiveStart<O> task) {
            this.onStart = task;
            return this;
        }

        public PrimitiveBuilder<O> setOnTick(GenericPrimitiveTask.PrimitiveTick<O> task) {
            this.onTick = task;
            return this;
        }

        public PrimitiveBuilder<O> setOnEnd(GenericPrimitiveTask.PrimitiveEnd<O> task) {
            this.onEnd = task;
            return this;
        }

        @Override
        GenericPrimitiveTask<O> build() {
            return new GenericPrimitiveTask<>(identifier, preconditions, effects, onStart, onTick, onEnd);
        }

    }

    public static class CustomBuilder<O extends TaskOwner, T extends BaseTask<O>> extends BehaviorTreeBuilder<O, T> {

        private final TaskFactory<O,T> factory;

        public CustomBuilder(String identifier, TaskFactory<O,T> supplier, Map<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions) {
            super(identifier);
            this.factory = supplier;
            preconditions.forEach((type, val) -> {
                addPrecondition(this, type, val);
            });
        }

        @Override
        T build() {
            return factory.get(this.preconditions);
        }

        public interface TaskFactory<O extends TaskOwner, T extends BaseTask<O>> {
            T get(Map<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions);
        }
    }
}
