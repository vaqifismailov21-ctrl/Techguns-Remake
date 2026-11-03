package techguns.capabilities.compat;

import net.minecraft.core.Direction;

/** Block entities that expose capabilities the 1.20.1 way; {@link Capabilities#register} wires them to NeoForge. */
public interface ICapabilityHost {
    default <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) { return LazyOptional.empty(); }
    default <T> LazyOptional<T> getCapability(Capability<T> capability) { return getCapability(capability, null); }
    default void invalidateCaps() {}
    default void reviveCaps() {}
}
