package com.mialliance.mind.implementations.sensors;

import com.mialliance.entities.AbstractMi;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.sensors.BaseSensor;
import com.mialliance.registers.ModMemoryModules;
import com.mialliance.utils.WorldUtils;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OfficerSynchronizationSensor extends BaseSensor<AbstractMi> {

    // 200 * 2 = 400 Ticks
    //  400 / 20 == 20 Seconds
    //  Probably should increase this.
    private static final int SYNC_ATTEMPTS = 200;

    private final MemoryManager manager;
    private final Map<Integer, AbstractMi> syncMap;
    private final Map<Integer, Integer> syncAttempts;

    public OfficerSynchronizationSensor(@NotNull AbstractMi owner) {
        super(owner);
        this.manager = owner.getAgent().getMemories();
        this.syncMap = new HashMap<>();
        this.syncAttempts = new HashMap<>();
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
        this.manager.removeMemory(ModMemoryModules.IS_OFFICER);
    }

    @Override
    public void onTick() {
        List<Integer> ids = this.manager.getMemory(ModMemoryModules.SUBORDINATES);

        // If either necessary value is missing, abandon and wait to next tick
        if (ids == null) {
            this.evaluateMemories();
            return;
        }

        // Validate Subordinates are alive, and clear those that are dead
        ids.forEach(id -> {
            // Attempt to get Subordinate reference
            AbstractMi ref;
            if (!syncMap.containsKey(id)) {
                // End early if there is no Mi found as a Subordinate
                ref = WorldUtils.getMi(id, owner.level);
                if (ref == null) {
                    Integer attempt = syncAttempts.get(id);
                    if (attempt == null) {
                        syncAttempts.put(id, 1);
                    } else if (attempt > SYNC_ATTEMPTS) {
                        // Remove subordinate if the Subordinate is lost or in an unknown state
                        //  RIP subordinate, 0/10, you will not be missed for you were dishonorably discharged
                        ids.remove(id);
                    } else {
                        syncAttempts.put(id, ++attempt);
                    }
                    return;
                }
                syncMap.put(id, ref);
            } else {
                ref = syncMap.get(id);
            }

            // Ensure that Subordinate is ours
            Integer officerID = ref.getAgent().getMemories().getMemory(ModMemoryModules.OFFICER);
            if (officerID == null || owner.getId() != officerID) {
                // Remove if not our subordinate
                //  Why the fuck are you here, get out of my house
                ids.remove(id);
                syncMap.remove(id);
            }

            // If Entity is removed, check Reason to see if they would be destroyed and cannot be returned.
            if (ref.isRemoved()) {
                Entity.RemovalReason reason = ref.getRemovalReason();
                assert reason != null;
                if (reason.shouldDestroy()) {
                    ids.remove(id);
                }
                syncMap.remove(id);
            }

        });
    }

    @Override
    public int ticksToCooldown() {
        return 2;
    }

    private void evaluateMemories() {
        if (!this.manager.hasMemory(ModMemoryModules.SUBORDINATES)) {
            this.manager.setMemory(ModMemoryModules.SUBORDINATES, new LinkedList<>());
        }
    }

}
