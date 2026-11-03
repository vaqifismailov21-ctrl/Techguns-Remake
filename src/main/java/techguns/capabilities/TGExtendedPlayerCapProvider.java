package techguns.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import techguns.Techguns;

public final class TGExtendedPlayerCapProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Techguns.MODID, "extended_player");
    public static final Capability<TGExtendedPlayer> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final TGExtendedPlayer data;
    private LazyOptional<TGExtendedPlayer> optional;
    public TGExtendedPlayerCapProvider(Player player) { data = new TGExtendedPlayer(player); optional = LazyOptional.of(() -> data); }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability != CAPABILITY) return LazyOptional.empty();
        // Entity's dispatcher rejects queries while invalid. After reviveCaps (respawn cloning),
        // revive the handle to the same saved data; invalidated handles stay invalid for consumers.
        if (!optional.isPresent()) optional=LazyOptional.of(() -> data);
        return optional.cast();
    }
    @Override public CompoundTag serializeNBT() { return data.saveToNBT(); }
    @Override public void deserializeNBT(CompoundTag tag) { data.loadFromNBT(tag); }
    public void invalidate() { optional.invalidate(); }
}
