package com.mialliance.mind.implementations.agents;

import com.mialliance.entities.AbstractMi;
import com.mialliance.mind.base.agents.BaseAgent;
import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.communication.CommListener;
import com.mialliance.mind.base.communication.Communication;
import com.mialliance.mind.base.communication.CommunicationTracker;
import com.mialliance.mind.base.events.CommunicationEvent;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.tasks.CompoundTask;
import com.mialliance.mind.implementations.communication.CommunicationPriority;
import com.mialliance.mind.implementations.communication.MiBoundCommIntent;
import com.mialliance.registers.ModMemoryModules;
import com.mialliance.utils.CommUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MiAgent extends BaseAgent<EntityMindOwner<AbstractMi>> {

    private final CommunicationTracker tracker;
    private final float defaultCommunicationRange;

    public MiAgent(@NotNull EntityMindOwner<AbstractMi> owner, @NotNull CompoundTask<EntityMindOwner<AbstractMi>> domain, float defaultCommunicationRange, long timeToRememberCommunications) {
        super(owner, domain);

        this.tracker = new CommunicationTracker(timeToRememberCommunications);
        this.defaultCommunicationRange = defaultCommunicationRange;
    }

    @Override
    public void onRecieveMessage(@NotNull Communication comm) {
        // If the communication is not from the same team, ignore-we DON'T want to listen to orders from enemies!
        if (!CommUtils.onSameTeam(comm.getOrigin(), this.getOwner().getEntity())) return;

        // If the communication is not meant for Mis, ignore.
        if (!(comm.getIntent() instanceof MiBoundCommIntent)) return;

        // If the Mi aleady has encountered the Communication and remembers it
        if (this.tracker.remembersCommunication(comm)) return;

        // If, for whatever reason, Components and Sensors accept Communications, check if Communication should be applied
        CommunicationEvent event = new CommunicationEvent(comm);
        this.emit(event);
        if (event.isCancelled()) return;

        // If it is a communication that is priority, force application
        if (comm.getIntent().priorityIntent()) {
            comm.applyToMemories(this.getMemories());
            return;
        }

        CommunicationPriority currentPriority = this.getMemories().getMemory(ModMemoryModules.CURRENT_COMMUNICATION_PRIORITY);
        if (currentPriority == null) currentPriority = CommunicationPriority.NONE;
        AtomicReference<CommunicationPriority> pri = new AtomicReference<>(null);
        for (CommunicationPriority commP : CommunicationPriority.values()) {
            if (pri.get() == null && commP.getCommunicationPair() == comm.getIntent().getClass()) {
                pri.set(commP);
            }
        }
        CommunicationPriority commPriority = pri.get();
        // Ignore if the Communication is using an improper priority to avoid overwriting
        if (commPriority == null) return;

        // If it is of higher or equal priority, respond accordingly and act.
        //  Additionally, remember this Communication such that it cannot accept the same Communication twice if relayed.
        if (commPriority.ordinal() <= currentPriority.ordinal()) {
            this.getMemories().setMemory(ModMemoryModules.CURRENT_COMMUNICATION_PRIORITY, commPriority);
            comm.applyToMemories(this.getMemories());
            this.tracker.addCommunication(comm);
        }
        // Otherwise, we discard the comm as unimportant, and do not act on it.
        return;
    }

    @Override
    public Set<CommListener> getListeners() {
        Float rangeMemory = this.getMemories().getMemory(ModMemoryModules.MESSAGE_DISPATCH_RANGE);
        float range = rangeMemory != null ? rangeMemory : defaultCommunicationRange;
        //noinspection UnnecessaryLocalVariable
        Set<CommListener> listeners = this.getOwner().getEntity().getLevel().getEntities(this.getOwner().getEntity(), AABB.ofSize(this.getOwner().getEntity().position(), range, range, range)).stream().filter(ent -> ent instanceof CommListener).map(ent -> (CommListener) ent).collect(Collectors.toSet());
        // If has Colony Memory, and is nearby Colony, add Colony to Listeners
        // TODO: If Mi is in any nearby colony (once ColonyManager is made), add that Colony to listeners IF IT'S THE SAME TEAM
        return listeners;
    }

    @Override
    protected void onTick() {
        // Tick and remove old Communications as needed
        this.tracker.tick();
        super.onTick();
    }

    @Override
    protected boolean shouldTick() {
        return !this.getOwner().getEntity().isNoAi();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        MemoryManager memoryManager = this.getMemories();
        // Always guarantee a communication priority for orders; If there is no serialized priority, create
        //  one at the Lowest priority possible.
        if (!memoryManager.hasMemory(ModMemoryModules.CURRENT_COMMUNICATION_PRIORITY)) {
            this.addMemory(ModMemoryModules.CURRENT_COMMUNICATION_PRIORITY, CommunicationPriority.NONE);
        }

        // TODO: Pull to AbstractMi instead of having here
        if (memoryManager.hasMemory(ModMemoryModules.ROGUE) && memoryManager.hasMemory(ModMemoryModules.COLONY)) {
            // TODO: Tell the Colony they're fucking off
            memoryManager.removeMemory(ModMemoryModules.COLONY);
        }

        // Don't want to be in a Group
        if (memoryManager.hasMemory(ModMemoryModules.LONER)) {
            if (memoryManager.hasMemory(ModMemoryModules.OFFICER)) {
                // TODO: Tell the Officer they're fucking off
                memoryManager.removeMemory(ModMemoryModules.OFFICER);
            }

            if (memoryManager.hasMemory(ModMemoryModules.SUBORDINATES)) {
                // TODO: Tell the Subordinates they're relieved of duty
                memoryManager.removeMemory(ModMemoryModules.SUBORDINATES);
            }
        }
    }

}
