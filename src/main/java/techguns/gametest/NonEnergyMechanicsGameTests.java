package techguns.gametest;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.items.guns.*;
import techguns.entities.projectiles.*;
import techguns.capabilities.TGExtendedPlayer;
@GameTestHolder("techguns") @PrefixGameTestTemplate(false)
public final class NonEnergyMechanicsGameTests {
    @GameTest(template="empty")
    public static void lockOnRequiresTrackingAndMissileSteersWithinSourceTurnLimit(GameTestHelper h) {
        var player=player(h);var gun=(GuidedMissileLauncher)TGuns.guidedmissilelauncher.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setYHeadRot(0);player.setXRot(0);
        var target=h.spawn(EntityType.IRON_GOLEM,new Vec3(2.5,2,7));target.setNoAi(true);
        for(int i=0;i<20;i++)gun.traceTarget(player);
        var state=TGExtendedPlayer.get(player);h.assertTrue(state.lockOnEntity==target&&state.lockOnTicks>=20,"Twenty uninterrupted tracking ticks must lock the source target");
        h.assertTrue(gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND),"Locked missile launcher must fire");
        var missile=(GuidedMissileProjectile)h.getLevel().getEntitiesOfClass(GenericProjectile.class,player.getBoundingBox().inflate(10),p->p.getShooter()==player).get(0);
        h.assertTrue(missile.target==target,"Server must attach the tracked entity to its missile");
        target.setPos(target.position().add(15,0,0));missile.setDeltaMovement(0,0,1);missile.tick();
        double angle=Math.acos(net.minecraft.util.Mth.clamp(missile.getDeltaMovement().normalize().dot(new Vec3(0,0,1)),-1,1));
        h.assertTrue(angle>0.01&&angle<=Math.toRadians(9)+0.00001,"Homing turns toward the target by at most nine degrees in one tick");missile.discard();h.succeed();
    }
    @GameTest(template="empty")
    public static void scopedShotStartsAtEyeCentreAndUnscopedShotUsesRightGunOffset(GameTestHelper h) {
        var player=player(h);var gun=TGuns.as50.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setYHeadRot(0);player.setXRot(0);
        gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND,false);
        var first=shots(h,player).get(0);h.assertTrue(Math.abs(first.getX()-player.getX())>0.15,"Unscoped rifle must originate at the source right-hand offset");first.discard();
        TGExtendedPlayer.get(player).setFireDelay(InteractionHand.MAIN_HAND,0);gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND,true);
        var aimed=shots(h,player).get(0);h.assertTrue(Math.abs(aimed.getX()-player.getX())<1E-8,"Scoped rifle must fire from the centre rather than from the hand");aimed.discard();h.succeed();
    }
    @GameTest(template="empty")
    public static void fuelMeleeConsumesOnceAndFlamingSwordIgnitesTarget(GameTestHelper h) {
        var player=player(h);var gun=(MiningGun)TGuns.shishkebap.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(15);
        var cow=h.spawn(EntityType.COW,new Vec3(2.5,2,3));cow.setNoAi(true);
        gun.onLeftClickEntity(stack,player,cow);
        h.assertTrue(gun.getCurrentAmmo(stack)==299,"A successful melee hit consumes exactly one fuel unit");
        h.assertTrue(cow.getHealth()<cow.getMaxHealth()&&cow.isOnFire(),"Fuel sword melee must damage and ignite its target");
        var hammer=(MiningGun)TGuns.powerhammer.get();var powered=hammer.getDefaultInstance();
        h.assertTrue(hammer.isCorrectToolForDrops(powered,Blocks.IRON_ORE.defaultBlockState()),"Powered pneumatic pickaxe must harvest iron");hammer.setAmmo(powered,0);
        h.assertFalse(hammer.isCorrectToolForDrops(powered,Blocks.IRON_ORE.defaultBlockState()),"Empty pneumatic tool must lose powered harvesting ability");h.succeed();
    }
    @GameTest(template="empty")
    public static void flamethrowerDamagesAndIgnitesButDiesInWater(GameTestHelper h) {
        var player=player(h);var gun=TGuns.flamethrower.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);gun.shootGunPrimary(stack,h.getLevel(),player,InteractionHand.MAIN_HAND);
        var flame=shots(h,player).get(0);h.assertTrue(flame instanceof FlamethrowerProjectile,"Fuel tank must create flame projectiles");
        var cow=h.spawn(EntityType.COW,new Vec3(2.5,2,4));cow.setNoAi(true);flame.setPos(h.absoluteVec(new Vec3(2.5,2.5,2)));flame.setDeltaMovement(0,0,3);flame.tick();
        h.assertTrue(cow.isOnFire()&&cow.getHealth()<cow.getMaxHealth(),"Flame projectile must cause source fire damage and ignite");
        var water=TGEntities.FLAME.get().create(h.getLevel());water.configure(5,0.5F,32,4,16,2,0,false);h.setBlock(1,2,1,Blocks.WATER);water.setPos(h.absoluteVec(new Vec3(1.5,2.5,1.5)));water.tick();h.assertTrue(water.isRemoved(),"Water extinguishes source flame projectiles");h.succeed();
    }
    @GameTest(template="empty")
    public static void blobDecaysAfterSixtyTicksAndLastStageDisappears(GameTestHelper h) {
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(2,2,2));var block=TGWeaponBlocks.BIOBLOB.get();h.getLevel().setBlockAndUpdate(pos,block.defaultBlockState().setValue(techguns.blocks.BioBlobBlock.SIZE,1));
        var blob=(techguns.tileentities.BioBlobBlockEntity)h.getLevel().getBlockEntity(pos);
        for(int i=0;i<60;i++)techguns.tileentities.BioBlobBlockEntity.tick(h.getLevel(),pos,h.getLevel().getBlockState(pos),blob);
        h.assertTrue(h.getLevel().getBlockState(pos).getValue(techguns.blocks.BioBlobBlock.SIZE)==0,"Blob must lose one growth stage in sixty ticks");
        for(int i=0;i<60;i++)techguns.tileentities.BioBlobBlockEntity.tick(h.getLevel(),pos,h.getLevel().getBlockState(pos),blob);h.assertTrue(h.getLevel().isEmptyBlock(pos),"Last blob stage must disappear in the following sixty ticks");h.succeed();
    }
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper h){var player=new net.minecraftforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"mechanics-test"));player.setPos(h.absoluteVec(new Vec3(2.5,2,1)));return player;}
    @GameTest(template="empty")
    public static void emptyMagazineCraftsStayEmptyAndGrimReaperSumsMissiles(GameTestHelper h) {
        var player=player(h);
        var emptyRecipe=h.getLevel().getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("techguns","pistol_alt")).orElseThrow();
        var grid=new net.minecraft.world.inventory.TransientCraftingContainer(player.inventoryMenu,3,3);
        var shaped=(net.minecraft.world.item.crafting.ShapedRecipe)emptyRecipe;
        for(int i=0;i<shaped.getIngredients().size();i++){var ingredient=shaped.getIngredients().get(i);if(!ingredient.isEmpty())grid.setItem(i,ingredient.getItems()[0].copy());}
        h.assertTrue(shaped.matches(grid,h.getLevel()),"Original empty-magazine pistol ingredients must craft successfully");
        var result=shaped.assemble(grid,h.getLevel().registryAccess());h.assertTrue(TGuns.pistol.get().getCurrentAmmo(result)==0,"Crafting with empty magazine must not create free ammunition");
        grid.clearContent();int i=0;
        for(int slot:new int[]{0,1,3,4}) {var launcher=TGuns.guidedmissilelauncher.get().getDefaultInstance();TGuns.guidedmissilelauncher.get().setAmmo(launcher,i++%2);grid.setItem(slot,launcher);}
        grid.setItem(5,TGItems.RECEIVER_CARBON.get().getDefaultInstance());
        var sum=(techguns.recipes.AmmoSumRecipe)h.getLevel().getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("techguns","grimreaper")).orElseThrow();
        h.assertTrue(sum.matches(grid,h.getLevel()),"Grim Reaper must accept four launchers with mixed remaining ammunition");
        var combined=sum.assemble(grid,h.getLevel().registryAccess());h.assertTrue(TGuns.grimreaper.get().getCurrentAmmo(combined)==2,"Four launchers contribute exactly their remaining two missiles");h.succeed();
    }
    @GameTest(template="empty")
    public static void everyGunReloadsItsSelectedVariantFromExtraAmmoSlots(GameTestHelper h) {
        int cases=0;
        for(var registered:TGuns.guns())for(var variant:registered.get().getAmmoType().getVariants()) {
            var player=player(h);var gun=registered.get();var stack=gun.getDefaultInstance();gun.setAmmo(stack,0);stack.getOrCreateTag().putString("ammovariant",variant.getKey());player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var ammo=gun.getAmmoType().getAmmo(gun.getCurrentAmmoVariant(stack));var extra=TGExtendedPlayer.get(player).tg_inventory;
            for(int i=0;i<ammo.length;i++)extra.setItem(7+i,ammo[i].copyWithCount(gun.getAmmoCount()>1?2:1));
            h.assertTrue(gun.tryForcedReload(stack,h.getLevel(),player,InteractionHand.MAIN_HAND),"Selected ammo must reload from special slots: "+registered.getId()+"/"+variant.getKey());
            h.assertTrue(gun.getCurrentAmmo(stack)==(gun.getAmmoCount()>1?2:gun.clipsize),"Reload must honor magazines, tanks and individually loaded shells/missiles");
            h.assertTrue(gun.getCurrentAmmoVariantKey(stack).equals(variant.getKey()),"Reload must preserve selected ammunition");cases++;
        }
        h.assertTrue(cases>40,"All non-energy gun ammunition variants must be covered");h.succeed();
    }
    @GameTest(template="empty")
    public static void sneakingDrillUsesSilkTouchAndConsumesOneFuel(GameTestHelper h) {
        var player=player(h);player.setShiftKeyDown(true);var gun=(MiningGun)TGuns.miningdrill.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        h.setBlock(2,2,3,Blocks.DIAMOND_ORE);var pos=h.absolutePos(new net.minecraft.core.BlockPos(2,2,3));
        h.assertTrue(gun.onBlockStartBreak(stack,pos,player),"Sneaking drill must use its source silk harvest path");
        h.assertTrue(h.getLevel().isEmptyBlock(pos)&&gun.getCurrentAmmo(stack)==299,"Silk harvest removes one block and consumes one fuel unit");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2));
        h.assertTrue(drops.stream().anyMatch(e->e.getItem().is(Blocks.DIAMOND_ORE.asItem()))&&drops.stream().noneMatch(e->e.getItem().is(net.minecraft.world.item.Items.DIAMOND)),"Sneaking drill must drop ore itself rather than diamonds");h.succeed();
    }
    @GameTest(template="empty")
    public static void drillMinesOriginalThreeByThreePlaneForOneFuel(GameTestHelper h) {
        var player=player(h);player.setYRot(0);player.setYHeadRot(0);player.setXRot(0);var gun=(MiningGun)TGuns.miningdrill.get();var stack=gun.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        for(int x=1;x<=3;x++)for(int y=2;y<=4;y++)h.setBlock(x,y,3,Blocks.STONE);
        var centre=h.absolutePos(new net.minecraft.core.BlockPos(2,3,3));h.assertTrue(player.gameMode.destroyBlock(centre),"Drill must break the centre through the server's real block interaction path");
        for(int x=1;x<=3;x++)for(int y=2;y<=4;y++)h.assertTrue(h.getLevel().isEmptyBlock(h.absolutePos(new net.minecraft.core.BlockPos(x,y,3))),"Drill must remove the complete source three-by-three plane");
        h.assertTrue(gun.getCurrentAmmo(stack)==299,"Original plane mining consumes fuel only for the primary block");h.succeed();
    }
    private static java.util.List<GenericProjectile> shots(GameTestHelper h,net.minecraft.world.entity.player.Player player){return h.getLevel().getEntitiesOfClass(GenericProjectile.class,player.getBoundingBox().inflate(16),p->p.getShooter()==player&&!p.isRemoved());}
}
