package com.mialliance.mind.implementations.sensors;

import com.mialliance.entities.AbstractMi;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.sensors.BaseSensor;
import com.mialliance.registers.ModMemoryModules;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OfficerSynchronizationSensor extends BaseSensor<AbstractMi> {

    private static final int SYNC_ATTEMPTS = 10;

    private final MemoryManager manager;
    private final Map<Integer, Integer> syncMap;

    public OfficerSynchronizationSensor(@NotNull AbstractMi owner) {
        super(owner);
        this.manager = owner.getAgent().getMemories();
        this.syncMap = new HashMap<>();
    }

    @Override
    protected void register() {
        if (!this.manager.hasMemory(ModMemoryModules.IS_OFFICER)) {
            this.manager.setMemory(ModMemoryModules.IS_OFFICER, Unit.INSTANCE);
        }
        this.evaluateMemories();
    }

    @Override
    protected void unregister() {
        this.manager.removeMemory(ModMemoryModules.SUBORDINATES);
        this.manager.removeMemory(ModMemoryModules.SUBORDINATES_ENTITIES);
        this.manager.removeMemory(ModMemoryModules.IS_OFFICER);
    }

    @Override
    public void onTick() {
        List<Integer> ids = this.manager.getMemory(ModMemoryModules.SUBORDINATES);
        List<AbstractMi> references = this.manager.getMemory(ModMemoryModules.SUBORDINATES_ENTITIES);

        // If either necessary value is missing, abandon and wait to next tick
        if (ids == null) {
            this.evaluateMemories();
            return;
        } else if (references == null) {
            this.evaluateMemories();
            return;
        }

        if (ids.size() != references.size()) {
            List<Integer> missingReferences = new ArrayList<>(ids);
            // Remove Subordinates the Officer already accounts for
            references.forEach(mi -> {
                if (missingReferences.contains(mi.getId())) {
                    missingReferences.remove(mi.getId());
                }
            });
            // Populate missing references for Subordinates.
            missingReferences.forEach(id -> {
                Integer attempt = syncMap.get(id);
                if (attempt == null) {
                    // New Attempt to Synchronize with Entity
                    attempt = 1;
                } else {
                    // Has attempted before
                    attempt += 1;
                }
                Entity foundEnt = owner.getLevel().getEntity(id);
                if (foundEnt == null) {
                    if (attempt >= SYNC_ATTEMPTS) {
                        // Abandon the Mi and assume it has died or gotten lost-both bad things!
                        ids.remove(id);
                        return;
                    }
                    syncMap.put(id, attempt);
                    return;
                }
                if (!(foundEnt instanceof AbstractMi mi)) {
                    // Somehow, an invalid entity got added to the List!
                    ids.remove(id);
                    return;
                }
                // TODO: Check if the Mi's Officer is our own- New Subordinates are obtained via
                //  Communication requests
                MemoryManager agentManager = mi.getAgent().getMemories();
                Integer val;
                // Subordinate is already linked to this officer
                if (((val = agentManager.getMemory(ModMemoryModules.OFFICER)) != null && val == owner.getId())) {
                    // Add reference to the List, and exit early.
                    references.add(mi);
                    ids.remove(id);
                    return;
                }
                // Failed to find Subordinate, move to next Sync attempt.
                syncMap.put(id, attempt);
            });
        }

        // Validate Subordinates are alive, and clear those that are dead
        references.forEach(mi -> {
            if (mi.isDeadOrDying()) {
                ids.remove(mi.getId());
                references.remove(mi);
            }
        });
    }

    @Override
    public int ticksToCooldown() {
        return 2 * 20;
    }

    private void evaluateMemories() {
        if (!this.manager.hasMemory(ModMemoryModules.SUBORDINATES)) {
            this.manager.setMemory(ModMemoryModules.SUBORDINATES, new LinkedList<>());
        }
        if (!this.manager.hasMemory(ModMemoryModules.SUBORDINATES_ENTITIES)) {
            this.manager.setMemory(ModMemoryModules.SUBORDINATES_ENTITIES, new LinkedList<>());
        }
    }

}
