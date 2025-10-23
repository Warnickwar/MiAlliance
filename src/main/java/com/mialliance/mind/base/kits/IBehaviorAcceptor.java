package com.mialliance.mind.base.kits;

import com.mialliance.mind.implementation.kits.IBehaviorSlot;

public interface IBehaviorAcceptor {
    void accept(Behavior behavior, IBehaviorSlot slot);
}
