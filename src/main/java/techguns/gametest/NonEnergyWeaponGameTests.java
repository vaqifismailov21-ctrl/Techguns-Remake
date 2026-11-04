package techguns.gametest;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.capabilities.TGExtendedPlayer;
import techguns.entities.projectiles.*;
import techguns.items.guns.*;

@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public final class NonEnergyWeaponGameTests {
    @GameTest(template="empty")
    public static void incendiaryAmmoRequiresSwitchCraftThenReloadsFromExtraSlotsAndIgnites(GameTestHelper h) {
        var player=player(h);var gun=TGuns.pistol.get();var stack=gun.getDefaultInstance();gun.setAmmo(stack,0);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);var extra=TGExtendedPlayer.get(player).tg_inventory;
        extra.setItem(7,new ItemStack(TGItems.PISTOL_MAGAZINE_INCENDIARY.get(),2));
        h.assertFalse(gun.tryForcedReload(stack,h.getLevel(),player,InteractionHand.MAIN_HAND),"R must not silently switch from ordinary rounds to incendiary rounds");
        h.assertTrue(extra.getItem(7).getCount()==2,"Failed reload must preserve the incendiary magazines");
        var grid=new net.minecraft.world.inventory.TransientCraftingContainer(player.inventoryMenu,2,2);grid.setItem(0,stack);grid.setItem(3,TGItems.PISTOL_MAGAZINE_INCENDIARY.get().getDefaultInstance());
        var recipe=(techguns.recipes.AmmoSwitchRecipe)h.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("techguns","pistol_ammo_incendiary")).orElseThrow();
        h.assertTrue(recipe.matches(grid,h.getLevel()),"Weapon and incendiary magazine must match source switch craft");
        var switched=recipe.assemble(grid,h.getLevel().registryAccess());gun.setAmmo(switched,0);player.setItemInHand(InteractionHand.MAIN_HAND,switched);
        h.assertTrue(gun.tryForcedReload(switched,h.getLevel(),player,InteractionHand.MAIN_HAND),"Selected incendiary gun must reload from extra ammo slot");
        h.assertTrue(extra.getItem(7).getCount()==1&&gun.getCurrentAmmo(switched)==18,"Reload consumes one magazine and fills the gun");
        TGExtendedPlayer.get(player).setFireDelay(InteractionHand.MAIN_HAND,0);h.assertTrue(gun.shootGunPrimary(switched,h.getLevel(),player,InteractionHand.MAIN_HAND),"Reloaded incendiary pistol must shoot");
        var bullet=bullets(h,player).get(0);var cow=h.spawn(EntityType.COW,new net.minecraft.core.BlockPos(2,2,4));cow.setNoAi(true);
        bullet.setPos(h.absoluteVec(new Vec3(2.5,2.5,2)));bullet.setDeltaMovement(0,0,2);bullet.tick();
        h.assertTrue(cow.isOnFire()&&cow.getHealth()<cow.getMaxHealth(),"Crafted and reloaded incendiary rounds must damage and ignite a real target");h.succeed();
    }
    @GameTest(template="empty")
    public static void everyConventionalGunFiresAllOfItsAmmoVariants(GameTestHelper h) {
        int tested=0;
        for(var registered:TGuns.guns()) {
            var gun=registered.get();if(gun.getEnergyMode()!=null||gun instanceof GenericGunCharge||gun==TGuns.flamethrower.get()||gun==TGuns.rocketlauncher.get()||gun==TGuns.grenadelauncher.get())continue;
            for(var ammoVariant:gun.getAmmoType().getVariants()) {
                var variant=ammoVariant.getKey();
                var player=player(h);var stack=gun.getDefaultInstance();gun.setAmmo(stack,gun.clipsize);stack.getOrCreateTag().putString("ammovariant",variant);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);h.assertTrue(gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND),"Registered ballistic gun must fire: "+gun+"/"+variant);
                var shots=bullets(h,player);h.assertTrue(shots.size()==gun.getPelletCount(),"Each trigger must create the original pellet or burst count");
                h.assertTrue(gun.getCurrentAmmo(stack)==gun.clipsize-1,"Each trigger consumes one source ammo unit");
                for(var p:shots) {
                    if(variant.equals("incendiary"))h.assertTrue(p instanceof GenericProjectileIncendiary,"Incendiary family must select fire projectiles");
                    if(variant.equals("explosive"))h.assertTrue(p instanceof GenericProjectileExplosive,"AS50 explosive family must select explosive projectiles");
                    p.discard();
                }
                tested++;
            }
        }
        h.assertTrue(tested>=35,"All twenty conventional guns and their variants must be exercised");h.succeed();
    }
    @GameTest(template="empty")
    public static void minigunCannotBypassPerTickCadenceAndPulseBurstIsStaggered(GameTestHelper h) {
        var p=player(h);var gun=TGuns.minigun.get();var stack=gun.getDefaultInstance();p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        for(int i=0;i<30;i++)TGWeaponPackets.handle(p,new TGWeaponPackets.Shoot(InteractionHand.MAIN_HAND,false));
        h.assertTrue(gun.getCurrentAmmo(stack)==gun.clipsize-1&&bullets(h,p).size()==1,"Zero source delay must still permit only one minigun shot in a tick");bullets(h,p).forEach(Entity::discard);
        var q=player(h);var pulse=TGuns.pulserifle.get();var burst=pulse.getDefaultInstance();q.setItemInHand(InteractionHand.MAIN_HAND,burst);pulse.shootGunPrimary(burst,h.getLevel(),q,InteractionHand.MAIN_HAND);
        var shots=bullets(h,q);h.assertTrue(shots.size()==3&&shots.stream().map(Entity::position).distinct().count()==3,"Pulse rifle uses one trigger for three spatially staggered rounds");
        h.assertTrue(pulse.getCurrentAmmo(burst)==pulse.clipsize-1,"Three-round burst consumes one magazine unit as in source");shots.forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="empty")
    public static void rocketVariantsRetainSourceDamageSpeedAndRadiusModifiers(GameTestHelper h) {
        for(var variant:List.of("default","high_velocity","nuke")) {
            var p=player(h);var gun=TGuns.rocketlauncher.get();var stack=gun.getDefaultInstance();stack.getOrCreateTag().putString("ammovariant",variant);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            gun.shootGunPrimary(stack,h.getLevel(),p,InteractionHand.MAIN_HAND);var shot=bullets(h,p).get(0);var tag=shot.saveWithoutId(new net.minecraft.nbt.CompoundTag());
            h.assertTrue(shot instanceof RocketProjectile,"Rocket launcher must never spawn a conventional bullet");
            h.assertTrue(tag.getFloat("damage")== (variant.equals("nuke")?250:50),"Nuclear rocket must retain fivefold direct and blast damage");
            h.assertTrue(shot.speed==(variant.equals("high_velocity")?2:1),"HV rocket must retain doubled velocity");
            h.assertTrue(shot.getTicksToLive()==(variant.equals("high_velocity")?150:200),"HV source lifetime uses 0.75 range multiplier");shot.discard();
        }
        h.succeed();
    }
    @GameTest(template="empty")
    public static void launchedGrenadeReflectsAtHalfSpeedAndDecrementsBounceCount(GameTestHelper h) {
        var player=player(h);var shot=new GrenadeProjectile(h.getLevel(),player,30,0.5F,320,0,4,8,12,0,false,EnumBulletFirePos.CENTER,2,false,true);
        h.setBlock(2,2,3,Blocks.STONE);shot.setPos(h.absoluteVec(new Vec3(2.5,2.5,1)));shot.setDeltaMovement(0,0,3);h.getLevel().addFreshEntity(shot);shot.tick();
        var others=bullets(h,player).stream().filter(p->p instanceof GrenadeProjectile&&!p.isRemoved()).toList();h.assertTrue(shot.isRemoved()&&others.size()==1,"A bounce replaces the original grenade with one reflected projectile");
        var next=(GrenadeProjectile)others.get(0);h.assertTrue(next.getBounces()==1&&Math.abs(next.getDeltaMovement().z+1.5)<0.0001,"Source bounce loses half its speed and one remaining bounce");next.discard();h.succeed();
    }
    @GameTest(template="empty")
    public static void chargedBiogunConsumesThreeUnitsAndBlobOverfillExplodes(GameTestHelper h) {
        var player=player(h);var gun=(GenericGunCharge)TGuns.biogun.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        gun.releaseUsing(stack,h.getLevel(),player,gun.getUseDuration(stack)-30);h.assertTrue(gun.getCurrentAmmo(stack)==27,"Full bio charge consumes three units");
        var shot=(BioGunProjectile)bullets(h,player).get(0);h.assertTrue(shot.power==3,"Full charge must retain blob strength three");
        TGExtendedPlayer.get(player).enableSafemode=false;
        // The projectile was created under safe mode; use a second real charged shot for the block path.
        shot.discard();gun.releaseUsing(stack,h.getLevel(),player,gun.getUseDuration(stack)-30);shot=(BioGunProjectile)bullets(h,player).get(0);
        h.setBlock(2,2,3,Blocks.STONE);shot.setPos(h.absoluteVec(new Vec3(2.5,2.5,1)));shot.setDeltaMovement(0,0,3);shot.tick();
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(2,2,2));h.assertTrue(h.getLevel().getBlockEntity(pos) instanceof techguns.tileentities.BioBlobBlockEntity,"Bio projectile must place its real block and block entity");
        var blob=(techguns.tileentities.BioBlobBlockEntity)h.getLevel().getBlockEntity(pos);h.assertTrue(h.getLevel().getBlockState(pos).getValue(techguns.blocks.BioBlobBlock.SIZE)==2,"Charged blob starts at source size three");blob.hitBlob(1,player);
        h.assertTrue(h.getLevel().isEmptyBlock(pos),"Fourth blob unit detonates and removes the blob");h.succeed();
    }
    @GameTest(template="empty")
    public static void powerhammerChargeConsumesFiveAirUnitsAndMultipliesDamage(GameTestHelper h) {
        var player=player(h);var gun=(MiningGun)TGuns.powerhammer.get();var stack=gun.getDefaultInstance();stack.getOrCreateTag().putInt("miningHead",2);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        gun.releaseUsing(stack,h.getLevel(),player,gun.getUseDuration(stack)-20);h.assertTrue(gun.getCurrentAmmo(stack)==295,"Full hammer charge consumes five compressed-air units");
        var shots=bullets(h,player);h.assertTrue(shots.size()==1&&shots.get(0) instanceof ToolProjectile,"Charge creates the pneumatic attack projectile");
        h.assertTrue(shots.get(0).saveWithoutId(new net.minecraft.nbt.CompoundTag()).getFloat("damage")==22.5F,"Carbon head adds one damage before five-unit charge multiplication");shots.forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="empty")
    public static void miningHeadUpgradePreservesFuelAndCustomNbt(GameTestHelper h) {
        var player=player(h);var gun=(MiningGun)TGuns.chainsaw.get();var stack=gun.getDefaultInstance();gun.setAmmo(stack,17);stack.getOrCreateTag().putInt("miningHead",1);stack.getOrCreateTag().putString("preserved","original");
        var grid=new net.minecraft.world.inventory.TransientCraftingContainer(player.inventoryMenu,2,2);grid.setItem(1,stack);grid.setItem(2,TGItems.CHAINSAWBLADES_CARBON.get().getDefaultInstance());
        var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,grid,h.getLevel()).orElseThrow();var result=recipe.assemble(grid,h.getLevel().registryAccess());
        h.assertTrue(gun.getMiningHeadLevel(result)==2&&gun.getCurrentAmmo(result)==17&&result.getTag().getString("preserved").equals("original"),"Head upgrade must change only the head and preserve weapon state");h.succeed();
    }
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper h) {
        var player=new net.minecraftforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"nonenergy-test"));
        player.setPos(h.absoluteVec(new Vec3(2.5,2,1)));return player;
    }
    private static List<GenericProjectile> bullets(GameTestHelper h,net.minecraft.world.entity.player.Player player){return h.getLevel().getEntitiesOfClass(GenericProjectile.class,player.getBoundingBox().inflate(16),p->p.getShooter()==player&&!p.isRemoved());}
}
