package org.warnickwar.components.functional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.warnickwar.components.functional.events.ComponentEvent;

import java.util.Set;

public interface ComponentHandler<T> {

    T getParent();

    <C extends Component<?>> C getComponent(ComponentType<C, ?> type);

    boolean hasComponent(ComponentType<?,?> type);

    boolean isComponentLocked(ComponentType<?, ?> type);

    Set<Component<?>> getComponents();

    <E extends ComponentEvent> void emit(E event);

    boolean lock(ComponentType<?, ?> type);

    boolean unlock(ComponentType<?, ?> type);

    boolean addComponent(ComponentType<?, ?> type);

    boolean addPermanentComponent(ComponentType<?, ?> type);

    boolean removeComponent(ComponentType<?, ?> type);

    CompoundTag save(CompoundTag tag);

    void load(CompoundTag tag);

    FriendlyByteBuf write(FriendlyByteBuf buffer);

    void read(FriendlyByteBuf buffer);

    void tick();
}
