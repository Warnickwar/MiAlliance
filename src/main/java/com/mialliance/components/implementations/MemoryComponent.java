package com.mialliance.components.implementations;

import com.mialliance.components.Component;
import com.mialliance.components.ComponentObject;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.concurrent.atomic.AtomicReference;

// TODO: Move Manager functionality into Component later
public class MemoryComponent extends Component<ComponentObject> {

    private static final String DATA_KEY = "memories";

    private MemoryManager memories;

    public MemoryComponent() {
        super();
        this.memories = new MemoryManager();
    }

    public MemoryManager getMemories() {
        return this.memories;
    }

    @Override
    public void save(CompoundTag tag) {
        DataResult<Tag> res = MemoryManager.CODEC.encodeStart(NbtOps.INSTANCE, this.memories);
        AtomicReference<Tag> finalTag = new AtomicReference<>(null);
        res.get().ifLeft(finalTag::set);
        if (finalTag.get() != null) {
            tag.put(DATA_KEY, finalTag.get());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains(DATA_KEY)) {
            Tag memories = tag.get(DATA_KEY);
            Either<Pair<MemoryManager, Tag>, DataResult.PartialResult<Pair<MemoryManager, Tag>>> res = MemoryManager.CODEC.decode(NbtOps.INSTANCE, memories).get();
            res.ifLeft(pair -> this.memories = pair.getFirst());
        }
    }

}
