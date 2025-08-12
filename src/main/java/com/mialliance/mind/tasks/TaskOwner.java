package com.mialliance.mind.tasks;

import com.mialliance.mind.memories.MemoryManager;

public interface TaskOwner {

    <O extends TaskOwner> CompoundTask<O> getDomain();

    MemoryManager getMemories();
}
