package techguns.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ScreenEvent;
import techguns.TGWeaponPackets;
import techguns.TGuns;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.items.guns.GenericGun;
import techguns.items.guns.GenericGunCharge;

/**
 * QA client (-Dtechguns.verifyFireAll=true): opens a fresh flat survival world and fires every gun
 * through the same client path as a held mouse button, with a live target in front of the player.
 * Any exception in shooting, projectile ticking, effects or rendering crashes the client with a report.
 */
@EventBusSubscriber(modid = Techguns.MODID, value = Dist.CLIENT)
public final class WeaponFireVerifier {
    private static final int SETUP = 8, FIRE = 200, SETTLE = 20;
    private static int stage, gunIndex, ticks, titleFrames, shots, aimedShots;
    private static boolean minedLogged;
    private static int holdStart = -1, meleeHits;
    private WeaponFireVerifier() {}
    private static boolean enabled() { return Boolean.getBoolean("techguns.verifyFireAll"); }

    @SubscribeEvent public static void onboarding(ScreenEvent.Init.Post event) {
        if (!enabled() || !(event.getScreen() instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) return;
        var mc = Minecraft.getInstance(); mc.options.onboardAccessibility = false; mc.options.save();
        mc.tell(() -> mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen()));
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void title(ScreenEvent.Render.Post event) {
        if (!enabled() || stage != 0 || !(event.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen) || ++titleFrames < 2) return;
        stage = 1; Techguns.LOGGER.info("TECHGUNS_FIRE_ALL_START"); var mc = Minecraft.getInstance(); mc.options.pauseOnLostFocus = false;
        var rules = new GameRules(); rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null); rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        mc.createWorldOpenFlows().createFreshLevel("techguns-fire-" + System.currentTimeMillis(),
                new LevelSettings("Techguns weapon fire check", GameType.SURVIVAL, false, Difficulty.EASY, true, rules, WorldDataConfiguration.DEFAULT),
                new WorldOptions(1729L, false, false), access -> access.registryOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).createWorldDimensions());
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !enabled() || stage == 0) return;
        var mc = Minecraft.getInstance(); var player = mc.player; var server = mc.getSingleplayerServer();
        if (player == null || server == null || mc.level == null) return;
        if (mc.screen != null) mc.setScreen(null);
        if (stage == 1) { stage = 2; gunIndex = 0; ticks = 0; }
        var guns = Boolean.getBoolean("techguns.fireSpecialOnly") ? TGuns.guns().stream().filter(g -> g.get() instanceof GenericGunCharge || g.get() instanceof techguns.items.guns.MiningGun || !g.get().shootsWithLeftClick()).toList() : TGuns.guns();
        if (gunIndex >= guns.size()) {
            Techguns.LOGGER.info("TECHGUNS_FIRE_ALL_PASSED: {} guns fired {} times ({} aimed) through the client path, with reloads, projectiles, effects and rendering", guns.size(), shots, aimedShots);
            stage = 0; mc.stop(); return;
        }
        var gun = guns.get(gunIndex).get(); var id = guns.get(gunIndex).getId();
        ticks++;
        if (ticks == 1) {
            Techguns.LOGGER.info("TECHGUNS_FIRE_GUN: {} (shots so far {})", id, shots);
            server.execute(() -> {
                var sp = server.getPlayerList().getPlayers().get(0);
                sp.getAbilities().invulnerable = true; sp.onUpdateAbilities(); sp.setHealth(sp.getMaxHealth());
                sp.teleportTo(0.5, -60, 0.5); sp.setYRot(0); sp.setXRot(0); sp.setYHeadRot(0);
                var stack = new ItemStack(gun); gun.setAmmo(stack, 3);
                sp.getInventory().clearContent();
                sp.getInventory().selected = 0;                sp.setItemInHand(InteractionHand.MAIN_HAND, stack); sp.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                Techguns.LOGGER.info("TECHGUNS_FIRE_AMMO: {} -> {}", id, java.util.Arrays.toString(gun.getAmmoType().getAmmo(0)));
                for (var ammo : gun.getAmmoType().getAmmo(0)) if (!ammo.isEmpty()) { var give = ammo.copy(); give.setCount(Math.min(16, give.getMaxStackSize())); sp.getInventory().add(give); }
                sp.inventoryMenu.broadcastChanges();

                var level = sp.serverLevel();
                level.getEntitiesOfClass(Mob.class, sp.getBoundingBox().inflate(40)).forEach(m -> m.discard());
                var target = EntityType.ZOMBIE.spawn(level, new net.minecraft.core.BlockPos(0, -60, gun instanceof techguns.items.guns.MiningGun ? 2 : 7), MobSpawnType.COMMAND);
                if (target != null) { target.setNoAi(true); target.setPersistenceRequired(); }
            });
        }
        if (ticks < SETUP) return;
        var stack = player.getMainHandItem();
        if (stack.getItem() != gun) return;
        player.setYRot(0); player.setXRot(0);
        int t = ticks - SETUP;
        boolean melee = gun instanceof techguns.items.guns.MiningGun;
        boolean charge = gun instanceof GenericGunCharge;
        boolean guided = gun instanceof techguns.items.guns.GuidedMissileLauncher;
        // Second half: aimed use (scope / guided lock-on), the way a player uses these weapons.
        boolean aimPhase = t >= FIRE / 2 || Boolean.getBoolean("techguns.fireAimOnly");
        if (!melee && aimPhase && gun.canZoom()) {
            if (gun.isHoldZoom()) player.setShiftKeyDown(true);
            else if (!ClientZoom.isZoomed(player, gun)) ClientZoom.toggle(gun);
        }
        boolean aiming = gun.canZoom() && ClientZoom.isZoomed(player, gun);
        if (t < FIRE) {
            if (melee) {
                // Up close: melee hits on the target in the first half, then mining a block in the second half.
                if (t < FIRE / 2) {
                    var target = mc.level.getEntitiesOfClass(net.minecraft.world.entity.monster.Zombie.class, player.getBoundingBox().inflate(4)).stream().findFirst().orElse(null);
                    if (target != null && t % 12 == 0) { mc.gameMode.attack(player, target); player.swing(InteractionHand.MAIN_HAND); shots++; meleeHits++; }
                    if (t == FIRE / 2 - 1) Techguns.LOGGER.info("TECHGUNS_FIRE_MELEE: {} {} swings, target {}", id, meleeHits, target == null ? "killed" : "health " + target.getHealth());
                } else {
                    var pos = new net.minecraft.core.BlockPos(0, -60, 1);
                    if (t == FIRE / 2) server.execute(() -> { var lvl = server.overworld(); lvl.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(pos).inflate(6)).forEach(m -> m.discard()); lvl.setBlockAndUpdate(pos, (((techguns.items.guns.MiningGun) gun).tool() == techguns.items.guns.MiningGun.Tool.CHAINSAW ? net.minecraft.world.level.block.Blocks.OAK_LOG : net.minecraft.world.level.block.Blocks.STONE).defaultBlockState()); });
                    else if (t > FIRE / 2 + 3 && !mc.level.getBlockState(pos).isAir()) {
                        player.setXRot(30);
                        if (t == FIRE / 2 + 4) mc.gameMode.startDestroyBlock(pos, net.minecraft.core.Direction.NORTH);
                        else mc.gameMode.continueDestroyBlock(pos, net.minecraft.core.Direction.NORTH);
                        player.swing(InteractionHand.MAIN_HAND);
                    } else if (t > FIRE / 2 + 3 && mc.level.getBlockState(pos).isAir() && !minedLogged) { minedLogged = true; shots++; Techguns.LOGGER.info("TECHGUNS_FIRE_MINED: {}", id); }
                }
            } else if (charge || !gun.shootsWithLeftClick()) {
                // Charge weapons: one quick tap, then a full hold (full charge / lock-on) before release.
                int hold = charge ? (int) Math.ceil(((GenericGunCharge) gun).fullChargeTime) + (guided ? 40 : 5) : 15;
                int local = t - (aimPhase ? FIRE / 2 : 0);
                if (local == 0) { holdStart = -1; if (!player.isUsingItem()) { mc.gameMode.useItem(player, InteractionHand.MAIN_HAND); shots++; if (aiming) aimedShots++; } }
                else if (local == 4) { if (player.isUsingItem()) mc.gameMode.releaseUsingItem(player); }
                else if (local > 4 && holdStart < 0) {
                    var st = TGExtendedPlayer.find(player).orElse(null);
                    if (st != null && st.getFireDelay(InteractionHand.MAIN_HAND) <= 0 && !player.isUsingItem()) { mc.gameMode.useItem(player, InteractionHand.MAIN_HAND); if (player.isUsingItem()) { holdStart = t; shots++; if (aiming) aimedShots++; } }
                } else if (holdStart >= 0 && player.isUsingItem() && t - holdStart >= hold) {
                    mc.gameMode.releaseUsingItem(player); Techguns.LOGGER.info("TECHGUNS_FIRE_CHARGED: {} held {} ticks{}", id, hold, aiming ? " aimed" : ""); holdStart = -2;
                }
            } else {
                var state = TGExtendedPlayer.find(player).orElse(null);
                if (state != null && state.getFireDelay(InteractionHand.MAIN_HAND) <= 0) {
                    TGWeaponPackets.request(InteractionHand.MAIN_HAND, false, aiming);
                    if (gun.shootGunPrimary(stack, player.level(), player, InteractionHand.MAIN_HAND, aiming)) { shots++; if (aiming) aimedShots++; }
                }
            }
        } else if (t == FIRE) {
            if (player.isUsingItem()) mc.gameMode.releaseUsingItem(player);
            if (melee && !minedLogged) Techguns.LOGGER.info("TECHGUNS_FIRE_NOT_MINED: {} block {}", id, mc.level.getBlockState(new net.minecraft.core.BlockPos(0, -60, 1)));
            ClientZoom.reset(); player.setShiftKeyDown(false); minedLogged = false; meleeHits = 0; holdStart = -1;
        } else if (t >= FIRE + SETTLE) {
            Techguns.LOGGER.info("TECHGUNS_FIRE_GUN_DONE: {} ammo left {} inventory {}", id, gun.getCurrentAmmo(stack), player.getInventory().items.stream().filter(s -> !s.isEmpty()).map(s -> s.getCount() + "x" + s.getItem()).toList());
            gunIndex++; ticks = 0; }
    }
}
