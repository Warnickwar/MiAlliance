/**
 * <p>
 *      The Functional Components package handles all custom capabilities of a Component system used in the mod, in regard to Minecraft.
 *      The basis of the system runs on {@link org.warnickwar.components.functional.GenericComponentHandler ComponentHandlers}, {@link org.warnickwar.components.functional.Component Components}, and {@link org.warnickwar.components.functional.ComponentType ComponentTypes}.
 *      Functional Components will only ever run on the <b>Server Side</b>, so care should be taken to avoid Handlers and Components ticking on the Client side.
 * </p>
 * <h1>Component Handlers</h1>
 * <p>
 *     Components are managed and created, altered, and run in {@link org.warnickwar.components.functional.GenericComponentHandler ComponentHandlers}, where the type of the Handler determines the
 *     resulting "Parent" object, or object that is holding the Handler and Components. This allows for Components to work for a variety of conditions, including custom or prebuilt
 *     conditions, without the need for excessive casting on the Developer's part.
 * </p>
 * <p>
 *     When making a new {@link org.warnickwar.components.functional.GenericComponentHandler GenericComponentHandler}, it is highly advised to do so in
 *     its own class rather than making an anonymous class to handle such. Additionally, when implementing {@link org.warnickwar.components.functional.GenericComponentHandler ComponentHandlers} in objects, it is highly
 *     advised to implement {@link org.warnickwar.components.functional.ComponentHolder ComponentHolder} as a way of accessing and identifying
 *     that a {@link org.warnickwar.components.functional.GenericComponentHandler GenericComponentHandler} is available, and accessible.
 * </p>
 * <p>
 *     In the event of Synchronization and/or Serialization being required, such as in the case of Entities, {@link org.warnickwar.components.functional.GenericComponentHandler ComponentHandlers} natively have the capability of
 *     serializing and deserializing the Components for saving to CompoundTags with {@link org.warnickwar.components.functional.GenericComponentHandler#save(net.minecraft.nbt.CompoundTag)} and {@link org.warnickwar.components.functional.GenericComponentHandler#load(net.minecraft.nbt.CompoundTag)},
 *     or for synchronization over the network with {@link org.warnickwar.components.functional.GenericComponentHandler#write(net.minecraft.network.FriendlyByteBuf)} and {@link org.warnickwar.components.functional.GenericComponentHandler#read(net.minecraft.network.FriendlyByteBuf)}. Classes extending
 *     GenericComponentHandler can implement and add on to plenty of functions which allow for Handlers to have more capabilities, such as advanced synchronization and responses.
 *     One thing to keep in mind is that Synchronization should only account for <b>variables</b> of Components, not demanding any ticking functions.
 * </p>
 * <h1>
 *     Component Events
 * </h1>
 * <p>
 *     While Components can access other components through their respective handlers, they may want to share or release information with disregard for what Components respond.
 *     As a result, Functional Components natively have a system to encourage using events. All Component Events are derived from {@link org.warnickwar.components.functional.events.ComponentEvent ComponentEvent}, which serves
 *     as a boilerplate for classes which are indicated to be Events. By default, Events have no parameters, so it may be confusing how Events share information among Components.
 * </p>
 * <p>
 *     {@link org.warnickwar.components.functional.events.ComponentEvent ComponentEvents} do not work with a specific registry. Instead, ComponentEvents utilize the Class indicator of each event that comes in
 *     to sort, distribute, and relay events to corresponding Components. By default, {@link org.warnickwar.components.functional.GenericComponentHandler ComponentHandlers} are backed by a 2D Map of Components and Event Types,
 *     which allows for quick insertion and removal of Components from the Handler as necessary. Other implementations, though possible, are unnecessary.
 * </p>
 * <p>
 *     There are two ways of registering listeners to the Event Handlers currently, and each Component can only have one listener for an Event. One way is by manually
 *     overriding {@link org.warnickwar.components.functional.Component#getEventFunctions()} and merging a custom map with the superclass call. This can be tedious, however,
 *     so an annotation is provided to simplify the work- {@link org.warnickwar.components.functional.annotations.EventListener @EventListener} can be applied to any function and automatically registers the Listener to the map.
 *     Keep in mind that the annotation requires the function to <b>return void</b>, have <b>one</b> parameter, and for that parameter to <b>match</b> the Event class in the annotation. Not following the format will result in the listener
 *     being discarded.
 * </p>
 * <h1>
 *     Components
 * </h1>
 * <p>
 *     {@link org.warnickwar.components.functional.Component Components} are the backbone of the system, serving as the baseline class for all Components
 *     to continue from. Those that use Unity's MonoBehavior scripts may find the format of the {@link org.warnickwar.components.functional.Component Component} familiar, as they were made
 *     with heavy inspiration of Unity's Component system. Thus, those that are used to Unity's system should find little challenges when developing their own Components with this system.
 * </p>
 * <p>
 *     {@link org.warnickwar.components.functional.Component Components}, unlike Unity's system, have a type. This type dictates the type of {@link org.warnickwar.components.functional.GenericComponentHandler GenericComponentHandler}, at minimum,
 *     that the {@link org.warnickwar.components.functional.Component Component} is allowed to work for. These Components can be applied to handlers which inherit from the targeted {@link org.warnickwar.components.functional.GenericComponentHandler GenericComponentHandler},
 *     but cannot work for Handlers lesser or otherwise unrelated to the typed Handler.
 * </p>
 * @see org.warnickwar.components.functional.GenericComponentHandler Component Handler
 * @see org.warnickwar.components.functional.Component Component
 * @see org.warnickwar.components.functional.ComponentType Component Type
 * @see org.warnickwar.components.functional.events.ComponentEvent ComponentEvent
 */

package org.warnickwar.components.functional;