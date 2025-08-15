package com.mialliance.mind.implementations.communication;

import com.mialliance.mind.base.communication.CommIntent;
import com.mialliance.utils.ExtraCodecs;
import com.mojang.serialization.Codec;

public enum CommunicationPriority {
    PLAYER(PlayerCommIntent.class),
    OFFICER(OfficerCommIntent.class),
    COLONY(ColonyMiCommIntent.class),
    NONE(MiCommIntent.class);

    public static final Codec<CommunicationPriority> CODEC = ExtraCodecs.enumCodec(CommunicationPriority.class);

    private final Class<?> commIntentPair;

    <T extends CommIntent> CommunicationPriority(Class<T> commPair) {
        this.commIntentPair = commPair;
    }

    public Class<?> getCommunicationPair() {
        return this.commIntentPair;
    }
}
