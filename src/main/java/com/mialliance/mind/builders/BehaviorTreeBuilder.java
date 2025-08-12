package com.mialliance.mind.builders;

import com.mialliance.mind.memories.MemoryValue;
import com.mialliance.mind.memories.TemplateValue;
import com.mialliance.mind.tasks.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class BehaviorTreeBuilder<T extends BaseTask> {

    protected final String identifier;
    protected final HashMap<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions;

    BehaviorTreeBuilder(String identifier) {
        this.identifier = identifier;
        this.preconditions = new HashMap<>();
    }

    public static RootBuilder start(String identifier) {
        return new RootBuilder(identifier);
    }

    protected static void addPrecondition(BehaviorTreeBuilder<?> builder, MemoryModuleType<?> type, Predicate<MemoryValue<?>> valueCheck) {
        builder.preconditions.put(type, valueCheck);
    }

    abstract T build();

    public static class RootBuilder extends CompoundBuilder {

        RootBuilder(String identifier) {
            super(identifier);
        }

        @Override
        public RootBuilder setState(CompoundState state) {
            return (RootBuilder) super.setState(state);
        }

        @Override
        public RootBuilder addChild(BehaviorTreeBuilder<?> builder) {
            return (RootBuilder) super.addChild(builder);
        }

        @Override
        public <T extends BaseTask> RootBuilder addChild(String identifier, Supplier<T> supplier, Map<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions) {
            return (RootBuilder) super.addChild(identifier, supplier, preconditions);
        }

        @Override
        public <T extends BaseTask> RootBuilder addChild(String identifier, Supplier<T> supplier) {
            return (RootBuilder) super.addChild(identifier, supplier);
        }

        public CompoundTask construct() {
            return this.build();
        }

    }

    public static class CompoundBuilder extends BehaviorTreeBuilder<CompoundTask> {
        protected CompoundState state;

        protected ArrayList<BehaviorTreeBuilder<?>> children;

        public CompoundBuilder(String identifier) {
            super(identifier);
            this.state = CompoundState.FALLBACK;
            this.children = new ArrayList<>();
        }

        public CompoundBuilder setState(CompoundState state) {
            this.state = state;
            return this;
        }

        public <T extends BaseTask> CompoundBuilder addChild(String identifier, Supplier<T> supplier, Map<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions) {
            return this.addChild(new CustomBuilder<>(identifier, supplier, preconditions));
        }

        public <T extends BaseTask> CompoundBuilder addChild(String identifier, Supplier<T> supplier) {
            return this.addChild(identifier, supplier, Map.of());
        }

        public CompoundBuilder addChild(BehaviorTreeBuilder<?> builder) {
            this.children.add(builder);
            return this;
        }

        @Override
        CompoundTask build() {
            return new CompoundTask(identifier, state, preconditions, new LinkedList<>(children.stream().map(BehaviorTreeBuilder::build).toList()));
        }

    }

    // I highly recommend you just make your own Primitive classes.
    public static class PrimitiveBuilder<O extends TaskOwner> extends BehaviorTreeBuilder<GenericPrimitiveTask<O>> {

        private GenericPrimitiveTask.PrimitiveRun<O> onStart;
        private GenericPrimitiveTask.PrimitiveTick<O> onTick;
        private GenericPrimitiveTask.PrimitiveRun<O> onEnd;

        Map<MemoryModuleType<?>, TemplateValue<?>> effects;

        @SuppressWarnings("unchecked")
        public PrimitiveBuilder(String identifier) {
            super(identifier);
            this.onStart = (GenericPrimitiveTask.PrimitiveRun<O>) GenericPrimitiveTask.PrimitiveRun.EMPTY;
            this.onTick = (GenericPrimitiveTask.PrimitiveTick<O>) GenericPrimitiveTask.PrimitiveTick.EMPTY;
            this.onEnd = (GenericPrimitiveTask.PrimitiveRun<O>) GenericPrimitiveTask.PrimitiveRun.EMPTY;
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

        public PrimitiveBuilder<O> setOnStart(GenericPrimitiveTask.PrimitiveRun<O> task) {
            this.onStart = task;
            return this;
        }

        public PrimitiveBuilder<O> setOnTick(GenericPrimitiveTask.PrimitiveTick<O> task) {
            this.onTick = task;
            return this;
        }

        public PrimitiveBuilder<O> setOnEnd(GenericPrimitiveTask.PrimitiveRun<O> task) {
            this.onEnd = task;
            return this;
        }

        @Override
        GenericPrimitiveTask<O> build() {
            return new GenericPrimitiveTask<>(identifier, preconditions, effects, onStart, onTick, onEnd);
        }

    }

    public static class CustomBuilder<T extends BaseTask> extends BehaviorTreeBuilder<T> {

        private final Supplier<T> factory;

        public CustomBuilder(String identifier, Supplier<T> supplier, Map<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions) {
            super(identifier);
            this.factory = supplier;
            preconditions.forEach((type, val) -> {
                addPrecondition(this, type, val);
            });
        }

        @Override
        T build() {
            return factory.get();
        }

    }
}
