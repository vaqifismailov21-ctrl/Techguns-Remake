package techguns.capabilities;

import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import techguns.Techguns;

/** 1.21.1: the Techguns player data is a NeoForge data attachment (was a Forge capability in 1.20.1). */
public final class TGExtendedPlayerCapProvider {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Techguns.MODID, "extended_player");
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Techguns.MODID);
    public static final Supplier<AttachmentType<TGExtendedPlayer>> EXTENDED_PLAYER = ATTACHMENTS.register("extended_player",
            () -> AttachmentType.builder(holder -> new TGExtendedPlayer((Player) holder))
                    .serialize(new IAttachmentSerializer<CompoundTag, TGExtendedPlayer>() {
                        @Override public TGExtendedPlayer read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            TGExtendedPlayer data = new TGExtendedPlayer((Player) holder);
                            data.loadFromNBT(tag);
                            return data;
                        }
                        @Override public CompoundTag write(TGExtendedPlayer data, HolderLookup.Provider provider) { return data.saveToNBT(); }
                    }).build());
    private TGExtendedPlayerCapProvider() {}
}
