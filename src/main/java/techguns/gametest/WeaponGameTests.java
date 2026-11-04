package techguns.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.TGEntities;
import techguns.TGItems;
import techguns.TGWeaponPackets;
import techguns.TGuns;
import techguns.Techguns;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.GenericProjectile;
import techguns.util.InventoryUtil;

@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public final class WeaponGameTests {
    @GameTest(template = "empty")
    public static void oneShotConsumesOneRoundAndSpamCannotBypassCooldown(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var stack = TGuns.pistol.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        TGWeaponPackets.handle(player, new TGWeaponPackets.Shoot(InteractionHand.MAIN_HAND, false));
        helper.assertTrue(TGuns.pistol.get().getCurrentAmmo(stack) == 17, "One accepted shot must consume one of eighteen rounds");
        helper.assertTrue(TGExtendedPlayer.get(player).getFireDelay(InteractionHand.MAIN_HAND) == 4, "Pistol must use the original four-tick fire delay");
        for (int i = 0; i < 20; i++) TGWeaponPackets.handle(player, new TGWeaponPackets.Shoot(InteractionHand.MAIN_HAND, false));
        helper.assertTrue(TGuns.pistol.get().getCurrentAmmo(stack) == 17, "Repeated packets cannot bypass the server fire delay");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void emptyPistolReloadsFromExtraAmmoSlot(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var gun = TGuns.pistol.get(); var stack = gun.getDefaultInstance(); gun.setAmmo(stack, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var inventory = TGExtendedPlayer.get(player).tg_inventory;
        inventory.setItem(7, new ItemStack(TGItems.PISTOL_MAGAZINE.get(), 2));
        TGWeaponPackets.handle(player, new TGWeaponPackets.Shoot(InteractionHand.MAIN_HAND, false));
        helper.assertTrue(gun.getCurrentAmmo(stack) == 18, "An empty pistol must reload eighteen shots from one magazine");
        helper.assertTrue(inventory.getItem(7).getCount() == 1, "Reloading consumes exactly one magazine");
        helper.assertTrue(TGExtendedPlayer.get(player).getFireDelay(InteractionHand.MAIN_HAND) == 31, "Reload delay must remain reloadtime minus minFiretime");
        helper.assertTrue(InventoryUtil.countItemInInv(inventory.inventory, TGItems.PISTOL_MAGAZINE_EMPTY.get().getDefaultInstance(), 7, 15) == 1,
                "Reloading must return one empty magazine");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void failedReloadPreservesAmmoAndInventory(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var gun = TGuns.pistol.get(); var stack = gun.getDefaultInstance(); gun.setAmmo(stack, 6);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertFalse(gun.tryForcedReload(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND), "No replacement magazine means no reload");
        helper.assertTrue(gun.getCurrentAmmo(stack) == 6, "Failed reload cannot discard remaining rounds");
        helper.assertTrue(TGExtendedPlayer.get(player).getFireDelay(InteractionHand.MAIN_HAND) == 0, "Failed reload cannot start a cooldown");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void forcedReloadReturnsOriginalBulletConversion(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var gun = TGuns.pistol.get(); var stack = gun.getDefaultInstance(); gun.setAmmo(stack, 12);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var inventory = TGExtendedPlayer.get(player).tg_inventory;
        inventory.setItem(7, TGItems.PISTOL_MAGAZINE.get().getDefaultInstance());
        helper.assertTrue(gun.tryForcedReload(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND), "A spare magazine must allow forced reload");
        helper.assertTrue(gun.getCurrentAmmo(stack) == 18, "Forced reload must fill the pistol");
        helper.assertTrue(InventoryUtil.countItemInInv(inventory.inventory, TGItems.PISTOL_ROUNDS.get().getDefaultInstance(), 7, 15) == 2,
                "Twelve remaining shots must return two original bullet items");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void handCooldownsAndCreativeAmmoAreIndependent(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var gun = TGuns.pistol.get(); var main = gun.getDefaultInstance(); var off = gun.getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, main); player.setItemInHand(InteractionHand.OFF_HAND, off);
        gun.shootGunPrimary(main, helper.getLevel(), player, InteractionHand.MAIN_HAND);
        gun.shootGunPrimary(off, helper.getLevel(), player, InteractionHand.OFF_HAND);
        helper.assertTrue(gun.getCurrentAmmo(main) == 17 && gun.getCurrentAmmo(off) == 17, "Both hands must be able to fire independently");
        TGExtendedPlayer.get(player).setFireDelay(InteractionHand.MAIN_HAND, 0);
        player.getAbilities().instabuild = true;
        gun.shootGunPrimary(main, helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(gun.getCurrentAmmo(main) == 17, "Creative shooting must preserve ammunition");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void wallStopsBulletBeforeEntityBehindIt(GameTestHelper helper) {
        helper.setBlock(2, 2, 1, Blocks.STONE);
        var cow = helper.spawn(EntityType.COW, new Vec3(3.5, 2, 1.5)); cow.setNoAi(true);
        var bullet = bullet(helper, new Vec3(0.5, 2.5, 1.5), new Vec3(4, 0, 0));
        bullet.tick();
        helper.assertTrue(bullet.isRemoved(), "A solid wall must stop the bullet");
        near(helper, cow.getHealth(), 10, "A bullet cannot hit an entity behind the first solid wall");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void bulletHitsEntityOnPathAndPreservesDummyHit(GameTestHelper helper) {
        var cow = helper.spawn(EntityType.COW, new Vec3(2.5, 2, 1.5)); cow.setNoAi(true);
        var bullet = bullet(helper, new Vec3(0.5, 2.5, 1.5), new Vec3(4, 0, 0));
        bullet.tick();
        helper.assertTrue(bullet.isRemoved(), "A bullet must be removed after hitting a living entity");
        near(helper, cow.getHealth(), 7.99F, "Original generic bullet deals its damage plus the 0.01 knockback dummy hit");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void bulletMovementDragGravityAndLifetime(GameTestHelper helper) {
        var bullet = bullet(helper, new Vec3(1, 3, 1), new Vec3(0.5, 0, 0));
        bullet.configure(2, 1, 2, 0, 0, 2, 0, false); bullet.setProjectileGravity(0.015);
        double x = bullet.getX(); bullet.tick();
        near(helper, (float) (bullet.getX() - x), 0.5F, "Position must advance before velocity drag");
        near(helper, (float) bullet.getDeltaMovement().x, 0.495F, "Air drag must preserve the original 0.99 multiplier");
        near(helper, (float) bullet.getDeltaMovement().y, -0.015F, "Gravity must apply after drag");
        helper.assertFalse(bullet.isRemoved(), "A two-tick projectile must survive its first tick");
        bullet.tick(); helper.assertTrue(bullet.isRemoved(), "Projectile must expire after exactly two updates");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void pistolDamageFalloffMatchesOriginalFinalSetter(GameTestHelper helper) {
        var bullet = bullet(helper, new Vec3(1, 3, 1), Vec3.ZERO);
        bullet.configure(8, 2, 20, 18, 25, 5, 0, false);
        near(helper, bullet.damageAtDistance(18), 8, "Pistol damage drop begins at eighteen blocks after its second original setDamageDrop call");
        near(helper, bullet.damageAtDistance(21.5), 6.5F, "Damage must interpolate linearly through the source range");
        near(helper, bullet.damageAtDistance(26), 5, "Pistol minimum damage must be five beyond twenty-five blocks");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void ammoSwitchRecipeCopiesNameAndSelectsIncendiary(GameTestHelper helper) {
        var player = new net.minecraftforge.common.util.FakePlayer(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "ammo-switch-test"));
        var input = new net.minecraft.world.inventory.TransientCraftingContainer(player.inventoryMenu, 2, 2);
        var gun = TGuns.pistol.get(); var stack = gun.getDefaultInstance(); gun.setAmmo(stack, 4);
        stack.setHoverName(net.minecraft.network.chat.Component.literal("Preserved name"));
        stack.getOrCreateTag().putByte("camo", (byte) 0);
        input.setItem(0, stack); input.setItem(3, TGItems.PISTOL_MAGAZINE_INCENDIARY.get().getDefaultInstance());
        var recipe = helper.getLevel().getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Techguns.MODID, "pistol_ammo_incendiary")).orElseThrow();
        helper.assertTrue(recipe instanceof techguns.recipes.AmmoSwitchRecipe, "Original ammo-switch recipe must be loaded with its custom serializer");
        var switched = (techguns.recipes.AmmoSwitchRecipe) recipe;
        helper.assertTrue(switched.matches(input, helper.getLevel()), "One pistol and one incendiary magazine must match regardless of grid positions");
        var result = switched.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(gun.getCurrentAmmo(result) == 18 && gun.getCurrentAmmoVariantKey(result).equals("incendiary"), "Ammo-switch craft must produce a loaded incendiary pistol");
        helper.assertTrue(result.hasCustomHoverName() && result.getHoverName().getString().equals("Preserved name"), "Switching ammo must preserve the weapon's custom name");
        result.getOrCreateTag().putString("independent", "result");
        helper.assertFalse(stack.getOrCreateTag().contains("independent"), "Crafted weapon cannot share mutable NBT with its input");
        helper.assertTrue(gun.getCurrentAmmo(stack) == 4, "Previewing a recipe cannot consume or reload its input gun");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void impactPacketPreservesFaceAndIncendiaryFlag(GameTestHelper helper) {
        for (var face : net.minecraft.core.Direction.values()) {
            float pitch = techguns.TGProjectilePackets.impactPitch(face), yaw = techguns.TGProjectilePackets.impactYaw(face);
            var outward = new Vec3(0,0,1).xRot((float)Math.toRadians(-pitch)).yRot((float)Math.toRadians(-yaw));
            var normal = Vec3.atLowerCornerOf(face.getNormal());
            helper.assertTrue(outward.distanceTo(normal) < 0.00001, "Impact particles must fly away from every hit face: " + face);
            var packet = new techguns.TGProjectilePackets.Impact((short) 3, 1.25, -8.5, 91.125, pitch, yaw, true);
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                techguns.TGProjectilePackets.encode(packet,buffer);
                helper.assertTrue(packet.equals(techguns.TGProjectilePackets.decode(buffer)), "Impact packet must retain position, material, normal and incendiary flag");
            } finally { buffer.release(); }
        }
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void originalImpactDefinitionsAndCurvesLoad(GameTestHelper helper) throws java.io.IOException {
        try (var stream = WeaponGameTests.class.getClassLoader().getResourceAsStream("assets/techguns/particles/impactfx.txt")) {
            helper.assertTrue(stream != null, "Original particle definition file must be packaged");
            var effects = techguns.client.particle.BulletImpactDefinition.read(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8));
            helper.assertTrue(effects.size() == 8, "All eight normal bullet impact systems must load");
            var sparks = effects.get("BulletImpactSparks");
            helper.assertTrue(sparks.frames == 4 && sparks.columns == 2 && sparks.additive, "Original spark sprite layout and blend must be preserved");
            near(helper,sparks.range("size").min(),1.9F,"Original spark size minimum");
            near(helper,sparks.range("size").max(),3.2F,"Original spark size maximum");
            helper.assertTrue(sparks.rgb(0) == 0xFF8000 && sparks.rgb(1) == 0, "Original spark color must fade from orange to black in HSB");
            var dust = effects.get("BulletImpactDust");
            near(helper,dust.alpha(0),0,"Dust must start transparent");
            near(helper,dust.alpha(0.25F),0.75F,"Dust must reach original peak opacity");
            near(helper,dust.alpha(0.625F),0.375F,"Dust alpha must interpolate across original control points");
            var velocity = sparks.velocity(new java.util.Random(1),-90,0);
            helper.assertTrue(velocity.y >= 0.1 && velocity.y <= 0.15 && Math.abs(velocity.x) < 0.00001 && Math.abs(velocity.z) < 0.00001,
                    "Original 2..3 units/second sparks must move 0.10..0.15 blocks/tick out of an upward face");
        }
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void automaticWeaponsRespectOriginalCadenceUnderPacketSpam(GameTestHelper helper) {
        for(var reference : java.util.List.of(TGuns.ak47,TGuns.thompson,TGuns.mac10)) {
            var gun=reference.get();var player=helper.makeMockSurvivalPlayer();var stack=gun.getDefaultInstance();
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            helper.assertFalse(gun.isSemiAuto(),"Automatic weapon must retain trigger while held: "+reference.getId());
            for(int tick=0;tick<12;tick++) {
                techguns.capabilities.CapabilityEventHandler.tick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.START,player));
                for(int packet=0;packet<10;packet++) TGWeaponPackets.handle(player,new TGWeaponPackets.Shoot(InteractionHand.MAIN_HAND,false));
            }
            int expectedShots=gun==TGuns.mac10.get()?6:4;
            helper.assertTrue(gun.getCurrentAmmo(stack)==gun.clipsize-expectedShots,"Original server cadence must limit twelve ticks of packet spam for "+reference.getId());
        }
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void revolversReloadFromOneRoundItemAndKeepIncendiaryVariant(GameTestHelper helper) {
        for(var reference:java.util.List.of(TGuns.revolver,TGuns.goldenrevolver)) {
            var gun=reference.get();var player=helper.makeMockSurvivalPlayer();var stack=gun.getDefaultInstance();gun.setAmmo(stack,0);
            stack.getOrCreateTag().putString("ammovariant","incendiary");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var inventory=TGExtendedPlayer.get(player).tg_inventory;
            inventory.setItem(7,new ItemStack(TGItems.PISTOL_ROUNDS_INCENDIARY.get(),2));
            helper.assertTrue(gun.tryForcedReload(stack,helper.getLevel(),player,InteractionHand.MAIN_HAND),"One bullet item must reload the source six-shot revolver");
            helper.assertTrue(gun.getCurrentAmmo(stack)==6 && inventory.getItem(7).getCount()==1,"Revolver reload must consume one round item for six shots");
            helper.assertTrue(gun.getCurrentAmmoVariantKey(stack).equals("incendiary"),"Reload must preserve ammunition variant");
            helper.assertTrue(TGExtendedPlayer.get(player).getFireDelay(InteractionHand.MAIN_HAND)==45-gun.minFiretime,"Revolver reload delay must follow original reload minus fire interval");
        }
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void twoHandedGunsBlockOffhandShotsButMac10AllowsThem(GameTestHelper helper) {
        var player=helper.makeMockSurvivalPlayer();var pistol=TGuns.pistol.get();var stack=pistol.getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND,TGuns.ak47.get().getDefaultInstance());player.setItemInHand(InteractionHand.OFF_HAND,stack);
        TGWeaponPackets.handle(player,new TGWeaponPackets.Shoot(InteractionHand.OFF_HAND,false));
        helper.assertTrue(pistol.getCurrentAmmo(stack)==18,"Offhand pistol must not fire alongside a two-handed rifle");
        player.setItemInHand(InteractionHand.MAIN_HAND,TGuns.mac10.get().getDefaultInstance());
        TGWeaponPackets.handle(player,new TGWeaponPackets.Shoot(InteractionHand.OFF_HAND,false));
        helper.assertTrue(pistol.getCurrentAmmo(stack)==17,"Original one-and-a-half-handed MAC-10 must allow an offhand pistol");
        player.setItemInHand(InteractionHand.MAIN_HAND,pistol.getDefaultInstance());
        var rifle=TGuns.ak47.get().getDefaultInstance();player.setItemInHand(InteractionHand.OFF_HAND,rifle);
        TGExtendedPlayer.get(player).setFireDelay(InteractionHand.OFF_HAND,0);
        TGWeaponPackets.handle(player,new TGWeaponPackets.Shoot(InteractionHand.OFF_HAND,false));
        helper.assertTrue(TGuns.ak47.get().getCurrentAmmo(rifle)==30,"A two-handed weapon must not fire from the offhand");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void ak47CamouflageWrapsAndPersistsWithoutSharingAmmoNbt(GameTestHelper helper) {
        var gun=TGuns.ak47.get();var stack=gun.getDefaultInstance();gun.setAmmo(stack,17);
        helper.assertTrue(gun.switchCamo(stack)==1 && gun.getCurrentAmmo(stack)==17,"AK camo switch must not change ammunition");
        var loaded=ItemStack.of(stack.save(new net.minecraft.nbt.CompoundTag()));
        helper.assertTrue(gun.getCurrentCamoIndex(loaded)==1 && gun.getCurrentAmmo(loaded)==17,"AK camo and ammo must survive saving");
        helper.assertTrue(gun.switchCamo(loaded)==0 && gun.getCurrentCamoIndex(stack)==1,"Camo wraps without sharing NBT");
        near(helper,gun.getPenetration(),0.5F,"AK must preserve original low penetration");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void goldenRevolverUpgradePreservesAmmoAndName(GameTestHelper helper) {
        var player=new net.minecraftforge.common.util.FakePlayer(helper.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"gold-upgrade-test"));
        var input=new net.minecraft.world.inventory.TransientCraftingContainer(player.inventoryMenu,3,3);
        var revolver=TGuns.revolver.get().getDefaultInstance(); TGuns.revolver.get().setAmmo(revolver,2);
        revolver.getOrCreateTag().putString("ammovariant","incendiary");
        revolver.setHoverName(net.minecraft.network.chat.Component.literal("Gold upgrade"));
        for(int i=0;i<9;i++) input.setItem(i,new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT));
        input.setItem(4,revolver);
        var recipe=helper.getLevel().getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("techguns","goldenrevolver")).orElseThrow();
        helper.assertTrue(recipe instanceof techguns.recipes.CopyNbtRecipe,"Golden revolver must use the source NBT-preserving recipe");
        var shaped=(techguns.recipes.CopyNbtRecipe)recipe;
        helper.assertTrue(shaped.matches(input,helper.getLevel()),"Eight gold ingots around one revolver must match");
        var result=shaped.assemble(input,helper.getLevel().registryAccess());
        helper.assertTrue(result.is(TGuns.goldenrevolver.get()) && TGuns.goldenrevolver.get().getCurrentAmmo(result)==2,"Upgrade must change the weapon without granting free ammunition");
        helper.assertTrue(result.getHoverName().getString().equals("Gold upgrade") && TGuns.goldenrevolver.get().getCurrentAmmoVariantKey(result).equals("incendiary"),"Upgrade must preserve name and ammunition variant");
        TGuns.goldenrevolver.get().setAmmo(result,1);
        helper.assertTrue(TGuns.revolver.get().getCurrentAmmo(revolver)==2,"Recipe output cannot mutate its input's ammunition");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void weaponSoundPacketKeepsShooterIdentityAndReloadMode(GameTestHelper helper) {
        var player=helper.makeMockSurvivalPlayer();
        for(boolean reload : new boolean[]{false,true}) {
            var original=new TGWeaponPackets.WeaponSound(player.getId(),player.getUUID(),
                    techguns.TGSounds.PISTOL_FIRE.get().getLocation(),reload);
            var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                original.encode(buffer);
                helper.assertTrue(TGWeaponPackets.WeaponSound.decode(buffer).equals(original),
                        "Observer audio must keep sound ID, shooter UUID and moving reload flag");
                helper.assertTrue(buffer.readableBytes()==0,"Weapon sound packet must consume its complete payload");
            } finally { buffer.release(); }
        }
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void observerGunAudioPreservesOriginalRotatingEntityOffset(GameTestHelper helper) {
        var player=helper.makeMockSurvivalPlayer();
        player.setPos(100,70,-200);
        float[] angles={0,90,180,270};
        double[][] offsets={{1,0},{0,1},{-1,0},{0,-1}};
        for(int i=0;i<angles.length;i++) {
            player.setYHeadRot(angles[i]);
            var position=techguns.util.GunSoundPosition.of(player);
            helper.assertTrue(position.distanceTo(new Vec3(100+offsets[i][0],70+player.getBbHeight()*0.5,-200+offsets[i][1]))<0.00001,
                    "Observer sound must preserve the original TGSound cos/sin gun offset at yaw "+angles[i]);
        }
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void shotgunsSpawnEightPelletsPerShellAndKeepIncendiaryVariant(GameTestHelper helper) {
        for(var registered:java.util.List.of(TGuns.sawedoff,TGuns.combatshotgun)) for(boolean incendiary:new boolean[]{false,true}) {
            var player=helper.makeMockSurvivalPlayer(); var gun=registered.get(); var stack=gun.getDefaultInstance();
            gun.setAmmo(stack,gun.clipsize);
            if(incendiary) stack.getOrCreateTag().putString("ammovariant","incendiary");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            helper.assertTrue(gun.shootGunPrimary(stack,helper.getLevel(),player,InteractionHand.MAIN_HAND),"Loaded shotgun must fire");
            var pellets=helper.getLevel().getEntitiesOfClass(GenericProjectile.class,player.getBoundingBox().inflate(3),p->p.getShooter()==player);
            helper.assertTrue(pellets.size()==8,"Source shotguns fire one accurate pellet plus seven spread pellets");
            helper.assertTrue(gun.getCurrentAmmo(stack)==gun.clipsize-1,"Eight pellets must consume exactly one shell");
            for(var pellet:pellets) {
                helper.assertTrue((pellet instanceof techguns.entities.projectiles.GenericProjectileIncendiary)==incendiary,
                        "All eight pellets must use the selected ammo variant");
                if(incendiary) helper.assertTrue(pellet.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getBoolean("fireTrail"),
                        "Shotgun incendiary pellets must preserve the original fire-effect flag in saved data");
                helper.assertTrue(pellet.getTicksToLive()==(int)Math.ceil(gun.maxRange/gun.speed),"Every pellet must use source scaled lifetime");
            }
            helper.assertTrue(pellets.stream().map(p->p.getDeltaMovement()).distinct().count()==8,"Pellets must have independently generated trajectories");
            helper.assertFalse(gun.shootGunPrimary(stack,helper.getLevel(),player,InteractionHand.MAIN_HAND),"Packet spam must not create another volley during cooldown");
            pellets.forEach(net.minecraft.world.entity.Entity::discard);
        }
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void shotgunReloadUsesAvailableShellsAndKeepsLoadedShells(GameTestHelper helper) {
        for(var registered:java.util.List.of(TGuns.sawedoff,TGuns.combatshotgun)) for(int available:new int[]{1,3,12}) {
            var gun=registered.get();var player=helper.makeMockSurvivalPlayer();var stack=gun.getDefaultInstance();
            int alreadyLoaded=gun.clipsize==8?5:0; gun.setAmmo(stack,alreadyLoaded);
            stack.getOrCreateTag().putString("ammovariant","incendiary");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var inventory=TGExtendedPlayer.get(player).tg_inventory;
            inventory.setItem(7,new ItemStack(TGItems.SHOTGUN_ROUNDS_INCENDIARY.get(),available));
            helper.assertTrue(gun.tryForcedReload(stack,helper.getLevel(),player,InteractionHand.MAIN_HAND),"Even one spare shell must allow partial reload");
            int used=Math.min(available,gun.clipsize-alreadyLoaded);
            helper.assertTrue(gun.getCurrentAmmo(stack)==alreadyLoaded+used,"Reload must retain loaded shells and add only available shells");
            helper.assertTrue(inventory.getItem(7).getCount()==available-used,"Reload must not consume shells beyond capacity");
            helper.assertTrue(gun.getCurrentAmmoVariantKey(stack).equals("incendiary"),"Partial reload must preserve selected variant");
            helper.assertTrue(TGExtendedPlayer.get(player).getFireDelay(InteractionHand.MAIN_HAND)==gun.reloadtime-gun.minFiretime,
                    "Partial reload must preserve original reload delay");
        }
        helper.succeed();
    }
    private static GenericProjectile bullet(GameTestHelper helper, Vec3 position, Vec3 motion) {
        var bullet = TGEntities.GENERIC_PROJECTILE.get().create(helper.getLevel());
        bullet.configure(2, 1, 10, 0, 0, 2, 0, false);
        bullet.setPos(helper.absoluteVec(position)); bullet.setDeltaMovement(motion); return bullet;
    }
    private static void near(GameTestHelper helper, float actual, float expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 0.0001F, message + ": expected " + expected + ", got " + actual);
    }
}
