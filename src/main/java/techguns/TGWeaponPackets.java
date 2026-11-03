package techguns;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import techguns.items.guns.GenericGun;

public final class TGWeaponPackets {
    public record Shoot(InteractionHand hand, boolean reload,boolean aiming) {
        public Shoot(InteractionHand hand,boolean reload) { this(hand,reload,false); }
    }
    public record Animation(int entityId, UUID uuid, InteractionHand hand, boolean reload, int ticks, int muzzleTicks,float charge) {
        public Animation(int entityId,UUID uuid,InteractionHand hand,boolean reload,int ticks,int muzzleTicks){this(entityId,uuid,hand,reload,ticks,muzzleTicks,0);}
    }
    public record WeaponSound(int entityId, UUID uuid, net.minecraft.resources.ResourceLocation sound, boolean reload) {
        public void encode(net.minecraft.network.FriendlyByteBuf buffer) {
            buffer.writeVarInt(entityId); buffer.writeUUID(uuid); buffer.writeResourceLocation(sound); buffer.writeBoolean(reload);
        }
        public static WeaponSound decode(net.minecraft.network.FriendlyByteBuf buffer) {
            return new WeaponSound(buffer.readVarInt(), buffer.readUUID(), buffer.readResourceLocation(), buffer.readBoolean());
        }
    }
    public static void register() {
        TGPackets.NETWORK.messageBuilder(Shoot.class, 4, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> { buffer.writeEnum(message.hand); buffer.writeBoolean(message.reload);buffer.writeBoolean(message.aiming); })
                .decoder(buffer -> new Shoot(buffer.readEnum(InteractionHand.class), buffer.readBoolean(),buffer.readBoolean()))
                .consumerMainThread((message, context) -> { var player = context.get().getSender(); if (player != null) handle(player, message); }).add();
        TGPackets.NETWORK.messageBuilder(Animation.class, 5, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((message, buffer) -> { buffer.writeVarInt(message.entityId); buffer.writeUUID(message.uuid); buffer.writeEnum(message.hand);
                    buffer.writeBoolean(message.reload); buffer.writeVarInt(message.ticks); buffer.writeVarInt(message.muzzleTicks);buffer.writeFloat(message.charge); })
                .decoder(buffer -> new Animation(buffer.readVarInt(), buffer.readUUID(), buffer.readEnum(InteractionHand.class), buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(),buffer.readFloat()))
                .consumerMainThread((message, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> techguns.client.ClientWeaponEvents.animate(message))).add();
        TGPackets.NETWORK.messageBuilder(WeaponSound.class, 6, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WeaponSound::encode).decoder(WeaponSound::decode)
                .consumerMainThread((message, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> techguns.client.audio.GunSounds.receive(message))).add();
    }
    public static void handle(Player player, Shoot message) {
        if (player.isSpectator() || !player.isAlive() || player.containerMenu != player.inventoryMenu) return;
        var stack = player.getItemInHand(message.hand);
        if (stack.getItem() instanceof GenericGun gun) {
            if (message.reload) {
                if(!gun.tryForcedReload(stack, player.level, player, message.hand) && gun.getAmmoType().hasMultipleVariants()) {
                    int current=gun.getCurrentAmmoVariant(stack);
                    for(int variant=0;variant<gun.getAmmoType().getVariants().size();variant++) if(variant!=current
                            && java.util.Arrays.stream(gun.getAmmoType().getAmmo(variant)).allMatch(ammo->techguns.util.InventoryUtil.canConsumeAmmoPlayer(player,ammo))) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("techguns.gun.tooltip.ammoswitch"),true);
                        break;
                    }
                }
            }
            else gun.shootGunPrimary(stack, player.level, player, message.hand,message.aiming);
        }
    }
    public static void request(InteractionHand hand, boolean reload) { TGPackets.NETWORK.sendToServer(new Shoot(hand, reload)); }
    public static void request(InteractionHand hand,boolean reload,boolean aiming) { TGPackets.NETWORK.sendToServer(new Shoot(hand,reload,aiming)); }
    public static void animate(ServerPlayer player, InteractionHand hand, boolean reload, int ticks, int muzzleTicks) {
        animate(player,hand,reload,ticks,muzzleTicks,0);
    }
    public static void animate(net.minecraft.world.entity.LivingEntity player,InteractionHand hand,boolean reload,int ticks,int muzzleTicks,float charge) {
        TGPackets.NETWORK.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new Animation(player.getId(), player.getUUID(), hand, reload, ticks, muzzleTicks,charge));
    }
    public static void sound(net.minecraft.world.entity.LivingEntity player, net.minecraft.sounds.SoundEvent sound, boolean reload) {
        // Own client plays immediately. Observers need the entity so reload audio can follow it.
        TGPackets.NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
                new WeaponSound(player.getId(), player.getUUID(), sound.getLocation(), reload));
    }
    private TGWeaponPackets() {}
}
