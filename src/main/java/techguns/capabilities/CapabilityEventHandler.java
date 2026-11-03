package techguns.capabilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import techguns.Techguns;
import techguns.TGPackets;
import techguns.TGRadiationSystem;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.events.TGAutoFoodHandler;
import techguns.items.armors.TGArmorBonus;
import techguns.items.armors.TGEquipmentBonuses;

@EventBusSubscriber(modid = Techguns.MODID)
public final class CapabilityEventHandler {
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        TGExtendedPlayer.get(event.getEntity()).copyFrom(TGExtendedPlayer.get(event.getOriginal()));
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
            data.foodleft = 0; data.lastSaturation = 0; data.addRadiation(-200); data.markDirty();
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
    @SubscribeEvent public static void tickPre(PlayerTickEvent.Pre event) {
        Player p = event.getEntity();
        if (!p.isAlive() || p.isRemoved()) return;
        var data = p.level().isClientSide ? TGExtendedPlayer.find(p).orElse(null) : TGExtendedPlayer.get(p);
        if(data==null) return;
        {
            if (data.fireDelayMainhand > 0) data.fireDelayMainhand--;
            if (data.fireDelayOffhand > 0) data.fireDelayOffhand--;
            if (data.loopSoundDelayMainhand > 0) data.loopSoundDelayMainhand--;
            if (data.loopSoundDelayOffhand > 0) data.loopSoundDelayOffhand--;
            if (data.swingSoundDelay > 0) data.swingSoundDelay--;
        }
    }
    @SubscribeEvent public static void tickPost(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (!p.isAlive() || p.isRemoved()) return;
        var data = p.level().isClientSide ? TGExtendedPlayer.find(p).orElse(null) : TGExtendedPlayer.get(p);
        if(data==null) return;
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = data.tg_inventory.getItem(slot);
            if (stack.getItem() instanceof ITGSpecialSlot item) item.onPlayerTick(stack, p);
        }
        if (p instanceof ServerPlayer player) {
            TGAutoFoodHandler.tick(player, data);
            TGRadiationSystem.tickPlayer(player, data);
            data.detectEquipmentChanges();
            if (data.needsSync()) {
                TGPackets.syncOwner(player); TGPackets.syncTracking(player); data.clearDirty();
            }
        }
    }
    private CapabilityEventHandler() {}
}
