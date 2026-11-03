package techguns.util;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;

/** FluidStack NBT helpers for 1.21 (registry-aware codecs). */
public final class TGFluids2 {
    public static FluidStack load(CompoundTag tag) { return tag == null || tag.isEmpty() ? FluidStack.EMPTY : FluidStack.parseOptional(TGItemNbt.registries(), tag); }
    public static CompoundTag save(FluidStack stack) { return stack.isEmpty() ? new CompoundTag() : (CompoundTag) stack.save(TGItemNbt.registries()); }
    public static CompoundTag save(FluidStack stack, CompoundTag into) { into.merge(save(stack)); return into; }
    private TGFluids2() {}
}
