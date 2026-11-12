package techguns.capabilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;
import techguns.TGPackets;
import techguns.TGRadiationSystem;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.events.TGAutoFoodHandler;
import techguns.items.armors.TGArmorBonus;
import techguns.items.armors.TGEquipmentBonuses;

@Mod.EventBusSubscriber(modid = Techguns.MODID)
public final class CapabilityEventHandler {
    @SubscribeEvent public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            var provider = new TGExtendedPlayerCapProvider(player);
            event.addCapability(TGExtendedPlayerCapProvider.ID, provider);
            event.addListener(provider::invalidate);
        }
    }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        try {
            TGExtendedPlayer.get(event.getEntity()).copyFrom(TGExtendedPlayer.get(event.getOriginal()));
        } finally { event.getOriginal().invalidateCaps(); }
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) { synchronize(event.getEntity()); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) { synchronize(event.getEntity()); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { synchronize(event.getEntity()); }
    private static void synchronize(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var data = TGExtendedPlayer.get(player);
            data.enforcePermissions();
            TGPackets.syncOwner(serverPlayer); TGPackets.syncTracking(serverPlayer); data.clearDirty();
        }
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer target) TGPackets.syncToObserver(target, observer);
    }
    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var data = TGExtendedPlayer.get(player);
            if (data != null) { data.lockOnEntity = null; data.lockOnTicks = 0; } // death forgets the missile lock
            data.foodleft = 0; data.lastSaturation = 0; data.addRadiation(-TGRadiationSystem.RADLOST_ON_DEATH); data.markDirty();
        }
    }
    @SubscribeEvent public static void drops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
        var inventory = TGExtendedPlayer.get(player).tg_inventory;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) event.getDrops().add(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack));
        }
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (!event.player.isAlive() || event.player.isRemoved()) return;
        var data = event.player.level().isClientSide ? TGExtendedPlayer.find(event.player).orElse(null) : TGExtendedPlayer.get(event.player);
        if(data==null) return;
        if (event.phase == TickEvent.Phase.START) {
            if (data.fireDelayMainhand > 0) data.fireDelayMainhand--;
            if (data.fireDelayOffhand > 0) data.fireDelayOffhand--;
            if (data.loopSoundDelayMainhand > 0) data.loopSoundDelayMainhand--;
            if (data.loopSoundDelayOffhand > 0) data.loopSoundDelayOffhand--;
            if (data.swingSoundDelay > 0) data.swingSoundDelay--;
            return;
        }
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = data.tg_inventory.getItem(slot);
            if (stack.getItem() instanceof ITGSpecialSlot item) item.onPlayerTick(stack, event.player);
        }
        if (event.player instanceof ServerPlayer player) {
            TGAutoFoodHandler.tick(player, data);
            techguns.events.TGAutoHealHandler.tick(player, data);
            TGRadiationSystem.tickPlayer(player, data);
            data.detectEquipmentChanges();
            if (data.needsSync()) {
                TGPackets.syncOwner(player); TGPackets.syncTracking(player); data.clearDirty();
            }
        }
    }
    private CapabilityEventHandler() {}
}
