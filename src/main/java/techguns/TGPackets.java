package techguns;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import techguns.capabilities.TGExtendedPlayer;
import techguns.gui.player.TGPlayerInventoryContainer;
import techguns.items.armors.TGArmorBonus;
import techguns.items.armors.TGEquipmentBonuses;
import techguns.keybind.TGKeybindsID;

/** Direction-restricted packets; inventory contents are only sent by the server. */
public final class TGPackets {
    private static final String PROTOCOL = "8";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Techguns.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public record OpenInventory() {}
    public record KeyPress(byte buttonId, boolean showMessage) {}
    public record PlayerSync(int entityId, UUID uuid, boolean full, CompoundTag data) {
        public static PlayerSync decode(FriendlyByteBuf buffer) {
            int id = buffer.readVarInt();
            UUID uuid = buffer.readUUID();
            boolean full = buffer.readBoolean();
            CompoundTag tag = buffer.readNbt();
            return new PlayerSync(id, uuid, full, tag == null ? new CompoundTag() : tag);
        }
        public void encode(FriendlyByteBuf buffer) {
            buffer.writeVarInt(entityId); buffer.writeUUID(uuid); buffer.writeBoolean(full); buffer.writeNbt(data);
        }
    }

    public static void register() {
        techguns.deatheffects.DeathEffectEvents.register();
        TGProjectilePackets.register();
        TGWeaponPackets.register();
        NETWORK.messageBuilder(OpenInventory.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> {}).decoder(buffer -> new OpenInventory())
                .consumerMainThread((message, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null && player.isAlive() && !player.isSpectator()) {
                        syncOwner(player);
                        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                                (id, inventory, owner) -> new TGPlayerInventoryContainer(id, inventory),
                                Component.translatable("techguns.extendedinventory")));
                    }
                }).add();
        NETWORK.messageBuilder(KeyPress.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> { buffer.writeByte(message.buttonId()); buffer.writeBoolean(message.showMessage()); })
                .decoder(buffer -> new KeyPress(buffer.readByte(), buffer.readBoolean()))
                .consumerMainThread((message, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null && player.isAlive()) handleKey(player, message);
                }).add();
        NETWORK.messageBuilder(PlayerSync.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayerSync::encode).decoder(PlayerSync::decode)
                .consumerMainThread((message, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> techguns.client.ClientPacketHandlers.apply(message))).add();
    }

    public static void requestInventory() { NETWORK.sendToServer(new OpenInventory()); }
    public static void sendBulletImpact(net.minecraft.world.entity.Entity projectile, net.minecraft.world.phys.BlockHitResult hit, net.minecraft.world.level.block.SoundType sound) {
        TGProjectilePackets.send(projectile, hit, sound);
    }
    public static void sendKey(byte key, boolean message) { NETWORK.sendToServer(new KeyPress(key, message)); }
    public static PlayerSync snapshot(ServerPlayer player, boolean full) {
        return new PlayerSync(player.getId(), player.getUUID(), full, TGExtendedPlayer.get(player).synchronizationData(full));
    }
    public static void syncOwner(ServerPlayer player) { NETWORK.send(PacketDistributor.PLAYER.with(() -> player), snapshot(player, true)); }
    public static void syncTracking(ServerPlayer player) { NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), snapshot(player, false)); }
    public static void syncToObserver(ServerPlayer target, ServerPlayer observer) {
        NETWORK.send(PacketDistributor.PLAYER.with(() -> observer), snapshot(target, false));
    }

    private static void handleKey(ServerPlayer player, KeyPress message) {
        TGExtendedPlayer data = TGExtendedPlayer.get(player);
        String label;
        boolean state;
        switch (message.buttonId()) {
            case TGKeybindsID.TOGGLE_NIGHTVISION -> {
                data.enableNightVision = !data.enableNightVision;
                if (!data.enableNightVision && player.hasEffect(MobEffects.NIGHT_VISION)) {
                    player.removeEffect(MobEffects.NIGHT_VISION);
                    player.level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), TGSounds.NIGHTVISION_OFF.get(), SoundSource.PLAYERS, 1, 1);
                } else if (data.enableNightVision && TGEquipmentBonuses.getBonus(player, TGArmorBonus.NIGHTVISION, false) > 0) {
                    player.level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), TGSounds.NIGHTVISION_ON.get(), SoundSource.PLAYERS, 1, 1);
                }
                label = "techguns.armorTooltip.nightvision"; state = data.enableNightVision;
            }
            case TGKeybindsID.TOGGLE_SAFEMODE -> {
                data.enableSafemode = !data.enableSafemode;
                data.enforcePermissions();
                label = "techguns.msg.safemode"; state = data.enableSafemode;
            }
            case TGKeybindsID.TOGGLE_STEP_ASSIST -> {
                data.enableStepAssist = !data.enableStepAssist;
                label = "techguns.armorTooltip.stepassist"; state = data.enableStepAssist;
            }
            case TGKeybindsID.TOGGLE_JETPACK -> {
                if (data.isJumpkeyPressed()) return;
                data.enableJetpack = !data.enableJetpack;
                label = "techguns.msg.enablejetpack"; state = data.enableJetpack;
            }
            case TGKeybindsID.TOGGLE_HUD -> {
                data.showTGHudElements = !data.showTGHudElements;
                label = "techguns.tgguitooltip.togglehud"; state = data.showTGHudElements;
            }
            case TGKeybindsID.JETPACK_BOOST_START -> { data.setJumpkeyPressed(true); return; }
            case TGKeybindsID.JETPACK_BOOST_STOP -> { data.setJumpkeyPressed(false); return; }
            case TGKeybindsID.JETPACK_FORWARD_START -> { data.isForwardKeyPressed = true; return; }
            case TGKeybindsID.JETPACK_FORWARD_STOP -> { data.isForwardKeyPressed = false; return; }
            default -> { return; }
        }
        data.markDirty();
        if (message.showMessage()) {
            player.displayClientMessage(Component.literal("§e[Techguns]: §f")
                    .append(Component.translatable("techguns.msg.keybindtogglechange"))
                    .append("§e [").append(Component.translatable(label)).append("] §f")
                    .append(Component.translatable("techguns.msg.keybindtogglechange2"))
                    .append(state ? " §a[" : " §4[")
                    .append(Component.translatable("techguns.container.info." + (state ? "on" : "off"))).append("]"), true);
        }
        syncOwner(player); syncTracking(player); data.clearDirty();
    }
    private TGPackets() {}
}
