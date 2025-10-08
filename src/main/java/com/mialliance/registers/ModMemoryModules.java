package com.mialliance.registers;

import com.mialliance.MiAlliance;
import com.mialliance.colonies.Colony;
import com.mialliance.communication.Communication;
import com.mialliance.mind.implementations.communication.CommunicationPriority;
import com.mialliance.mind.implementations.memories.ColonyReference;
import com.mialliance.utils.ExtraCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ModMemoryModules {

    // Doing this to avoid allocating unnecessary Codecs of the same type
    public static final Codec<Unit> UNIT_CODEC = Codec.unit(Unit.INSTANCE);

    // MARKER MODULES

    /**
     * A Memory used to swap between different two different Idling Behaviors.
     * More advanced Idling behaviors can be used by using a separate Memory, or an Integer memory.
     * <p>
     *     { {@code Temporary}, {@code Non-Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Unit> IDLE_HAS_MOVED = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "idle_has_moved"));

    /**
     * A Memory which indicates that a Mi is an {@code Officer}.
     * <p>
     *     { {@code Serializable} , {@code Officer} }
     * </p>
     */
    public static final MemoryModuleType<Unit> IS_OFFICER = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "is_officer"), UNIT_CODEC);

    /**
     * A Memory which indicates that a Mi is not intending to attack.
     * <p>
     *     { {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Unit> NONCOMBATANT = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "peaceful"), UNIT_CODEC);

    /**
     * A Memory which can only be applied via commands. This forces Mi to never attack.
     * <p>
     *     { {@code Serializable} , {@code Command} }
     * </p>
     */
    public static final MemoryModuleType<Unit> FORCED_NONCOMBATANT = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "forced_peaceful"), UNIT_CODEC);

    /**
     * A Memory which can only be applied via commands. This makes the Mi not retreat from battle, at any cost.
     * <p>
     *     { {@code Serializable} , {@code Command} }
     * </p>
     */
    public static final MemoryModuleType<Unit> NO_FEAR = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "unretreatable"), UNIT_CODEC);

    /**
     * A Memory which can only be applied via commands. This makes the Mi not available to be in a {@link Colony Colony}, and
     * makes the Mi immediately depart from whatever Colony it may be registered to, if any.
     * <p>
     *     { {@code Serializable} , {@code Command} }
     * </p>
     */
    public static final MemoryModuleType<Unit> ROGUE = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "no_colony_ever"), UNIT_CODEC);

    /**
     * A Memory which can only be applied via commands. This makes the Mi not look to be in an {@code Officer} Regiment
     * if the Mi is not an Officer, or abandon the current subordinates and avoid recruiting other Mis if they
     * are an Officer.
     * <p>
     *     { {@code Serializable} , {@code Command} }
     * </p>
     */
    public static final MemoryModuleType<Unit> LONER = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "loner"), UNIT_CODEC);

    // RECORD MODULES

    /**
     * A Memory which indicates what {@link CommunicationPriority Communications} the Mi will accept. This can be a temporary Memory, which indicates that a Mi would be
     * invalid to accept orders of lower priority until the memory expires. By default, every Mi has a Communication Priority
     * of 3 ({@link CommunicationPriority#NONE CommunicationPriority.NONE}). Communications can bypass priority by being marked as a {@code Priority Intent}.
     * <p>
     *     { {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<CommunicationPriority> CURRENT_COMMUNICATION_PRIORITY = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "communication_priority"), CommunicationPriority.CODEC);

    /**
     * A Memory which holds the UUID and BlockPos of the affiliated {@link Colony Colony}.
     * <p>
     *     { {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<ColonyReference> COLONY = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "colony"), ColonyReference.CODEC);

    /**
     * A Memory which holds the ID of the {@code Officer} of the Mi.
     * <p>
     *     { {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Integer> OFFICER = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "officer"), Codec.INT);

    /**
     * A Memory used by Officers which hold the IDs of Subordinates.
     * <p>
     *     { {@code Serializable}, {@code Officer} }
     * </p>
     */
    public static final MemoryModuleType<List<Integer>> SUBORDINATES = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "subordinates"), ExtraCodecs.mutableListCodec(Codec.INT));

    // COOLDOWN MODULES

    /**
     * A Memory indicating that the Shield usage is on cooldown for Legionnaire Mis.
     * <p>
     *     { {@code Temporary}, {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Unit> SHIELD_COOLDOWN = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "has_shielded"), UNIT_CODEC);

    /**
     * A Memory indicating that the Gun usage is on cooldown for Gunslinger and Sniper Mis.
     * <p>
     *     { {@code Temporary}, {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Unit> GUN_COOLDOWN = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "has_fired_generic_gun"), UNIT_CODEC);

    /**
     * A Memory indicating that the Debuff application is on cooldown for Wizard Mis.
     * <p>
     *     { {@code Temporary}, {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Unit> DEBUFF_COOLDOWN = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "has_debuffed_enemy"), UNIT_CODEC);

    /**
     * A Memory indicating that the Lunge Attack used by certain Mis is on cooldown.
     * <p>
     *     { {@code Temporary}, {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Unit> LUNGE_COOLDOWN = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "has_lunged"), UNIT_CODEC);

    /**
     * A Memory indicating that an Officer has recently called a Formation.
     * <p>
     *     { {@code Temporary}, {@code Serializable}, {@code Officer} }
     * </p>
     */
    public static final MemoryModuleType<Unit> FORMATION_COOLDOWN = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "has_called_formation"), UNIT_CODEC);

    // TODO: Make Cooldown Memory Modules for all the Chosen Abilities.

    // SETTING MODULES

    /**
     * A Memory used by Mis to determine how far their messages, if they send messages, can go. By default, Mis have a communication range defined
     * in each individual Agent, so changes to the Memory will override the dispatch range.
     * <p>
     *     { {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Float> MESSAGE_DISPATCH_RANGE = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "voice_volume"), Codec.FLOAT);

    /**
     * A Memory used by Mis to determine if they should be in Melee attacks, or Ranged attacks. If the Memory is not registered, assume that it is a Melee attack.
     * <p>
     *     { {@code Serializable} }
     * </p>
     */
    public static final MemoryModuleType<Boolean> IS_MELEE = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "is_melee"), Codec.BOOL);

    // CONVERSATION MODULES

    /**
     * A Memory used by Mi {@code Officers} to save which Mi they currently are asking to join them. If the memory expires
     * (or "times out"), the Officer assumes the answer to be {@code False}.
     * <p>
     *     { {@code Temporary}, {@code Non-Serializable}, {@code Officer} }
     * </p>
     * TODO: Make an Intent for Party Requests.
     * @see ModMemoryModules#PARTY_OFFICER_REQUEST_RESPONSE
     */
    public static final MemoryModuleType<Integer> PARTY_OFFICER_REQUEST_JOIN = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "requested_mi_to_join"));

    /**
     * A Memory used by Mi to respond to {@code Officers} requesting they join their battalion. A return value of
     * {@code True} indicates that the Mi accepts joining, and marks the Officer as their leader. A return value of {@code False}
     * indicates that the Mi refuses, for any reason.
     * <p>
     *     { {@code Temporary}, {@code Non-Serializable} }
     * </p>
     * @see ModMemoryModules#PARTY_OFFICER_REQUEST_JOIN
     */
    public static final MemoryModuleType<Boolean> PARTY_OFFICER_REQUEST_RESPONSE = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "requested_mi_to_join_response"));

    /**
     * A Memory used by Mis to request to {@code Officers} on if they can join the Officer's Regiment. This Memory has the ID of the asking Mi, or the Origin Mi.
     * If the request expires (or "times out"), the Mi assumes the answer to be {@code False}.
     * <p>
     *     { {@code Temporary}, {@code Non-Serializable} }
     * </p>
     * @see ModMemoryModules#PARTY_MI_REQUEST_JOIN_OFFICER_ID
     * @see ModMemoryModules#PARTY_MI_REQUEST_RESPONSE
     */
    public static final MemoryModuleType<Integer> PARTY_MI_REQUEST_JOIN = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "request_officer_to_join"));

    /**
     * A Memory used by Mis to hold the ID of an {@link ModMemoryModules#PARTY_MI_REQUEST_JOIN Join Request's} {@code Officer} recipient ID. This is used to
     * validate the final joining.
     * <p>
     *     { {@code Temporary}, {@code Non-Serializable} }
     * </p>
     * @see ModMemoryModules#PARTY_MI_REQUEST_JOIN
     * @see ModMemoryModules#PARTY_MI_REQUEST_RESPONSE
     */
    public static final MemoryModuleType<Integer> PARTY_MI_REQUEST_JOIN_OFFICER_ID = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "request_officer_to_join_officer"));

    /**
     * A Memory used by Mis to determine if the Subordinate can join. This should be altered by an {@code Officer} through a {@link Communication Communication},
     * and used as a response from said Officer on whether the Subordinate can be allocated to the Officer's resources.
     * <p>
     *     { {@code Temporary}, {@code Non-Serializable}, {@code Officer} }
     * </p>
     * @see ModMemoryModules#PARTY_MI_REQUEST_JOIN
     * @see ModMemoryModules#PARTY_MI_REQUEST_JOIN_OFFICER_ID
     */
    public static final MemoryModuleType<Boolean> PARTY_MI_REQUEST_RESPONSE = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "request_officer_to_join_response"));

    @SuppressWarnings("deprecation")
    private static <T> MemoryModuleType<T> register(ResourceLocation id, @Nullable Codec<T> codec) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, id, new MemoryModuleType<>(Optional.ofNullable(codec)));
    }

    private static <T> MemoryModuleType<T> register(ResourceLocation id) {
        return register(id, null);
    }

}
