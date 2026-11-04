package techguns.gametest;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.items.armors.*;
import techguns.capabilities.TGExtendedPlayer;
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class EquipmentGameTests {
    @GameTest(template="empty") public static void sourceArmorHudRoundsEachPartAndIncludesNativeArmorWithoutChangingDefense(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var nativeArmor=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);nativeArmor.setBaseValue(6);
        var helmet=TGEquipment.ARMOR.get("t1_scout_helmet").get();p.setItemSlot(EquipmentSlot.HEAD,helmet.getDefaultInstance());
        h.assertTrue(ArmorDisplay.points(p)==9,"HUD combines six native armor points with three rounded Techguns helmet points");
        h.assertTrue(p.getArmorValue()==6,"Displaying Techguns armor must not add native mitigation a second time");
        nativeArmor.setBaseValue(0);for(String id:new String[]{"steam_helmet","steam_chestplate","steam_leggings","steam_boots"}){var armor=TGEquipment.ARMOR.get(id).get();p.setItemSlot(armor.slot,armor.getDefaultInstance());}
        h.assertTrue(ArmorDisplay.points(p)==20,"Original per-piece rounding displays twenty points for the nineteen-point steam suit");h.succeed();
    }
    @GameTest(template="empty") public static void brokenArmorDisplayPreservesSourceDamageAndRepairBehavior(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var armor=TGEquipment.ARMOR.get("t1_combat_chestplate").get();var stack=armor.getDefaultInstance();p.setItemSlot(EquipmentSlot.CHEST,stack);
        var source=new techguns.damagesystem.TGDamageSource(p.damageSources().playerAttack(p));
        float protectedDamage=techguns.damagesystem.DamageSystem.applySuitArmor(p,source,10);h.assertTrue(protectedDamage<10&&ArmorDisplay.points(p)>0,"Working armor protects and displays points");
        stack.setDamageValue(stack.getMaxDamage()-1);int before=stack.getDamageValue();
        h.assertTrue(ArmorDisplay.points(p)==0,"Original getArmorDisplay hides broken armor points");
        h.assertTrue(techguns.damagesystem.DamageSystem.applySuitArmor(p,source,10)==protectedDamage,"Original getProperties retains typed protection even when display and bonuses are disabled");
        h.assertTrue(!stack.isEmpty()&&stack.getDamageValue()==before,"Broken armor remains available for repair");h.succeed();
    }
    @GameTest(template="empty") public static void gliderAddsSourceAirSteeringWithoutChangingVerticalLift(GameTestHelper h){var p=h.makeMockSurvivalPlayer();var state=TGExtendedPlayer.get(p);state.tg_inventory.setItem(1,TGAccessories.GLIDER.get().getDefaultInstance());p.setOnGround(false);p.setShiftKeyDown(true);p.setYRot(0);p.setDeltaMovement(0,-.4,0);AccessoryItem.glideSteering(p,0,1);h.assertTrue(Math.abs(p.getDeltaMovement().z-.03)<.000001&&p.getDeltaMovement().y==-.4,"Glider adds the source 0.03 air acceleration while leaving lift to the glide routine");p.setShiftKeyDown(false);p.setDeltaMovement(0,-.4,0);AccessoryItem.glideSteering(p,0,1);h.assertTrue(p.getDeltaMovement().z==0,"Releasing sneak removes extra steering");h.succeed();}
    @GameTest(template="empty") public static void sourceArmorRegistryRetainsDurabilityResistanceAndBrokenState(GameTestHelper h){
        h.assertTrue(TGEquipment.ARMOR.size()==57,"All 57 original wearable items must be registered");var p=h.makeMockSurvivalPlayer();
        for(var entry:TGEquipment.ARMOR.values()){
            var armor=entry.get();var stack=armor.getDefaultInstance();h.assertTrue(stack.getMaxDamage()>0&&armor.getCamoCount()>0,"Every armor part has its original durability and camouflage");
            p.setItemSlot(armor.slot,stack);stack.setDamageValue(stack.getMaxDamage()-2);stack.hurtAndBreak(2000,p,a->{});
            h.assertTrue(!stack.isEmpty()&&stack.getDamageValue()==stack.getMaxDamage()-1,"Broken TG armor is retained for repair");
            for(var bonus:TGArmorBonus.values())h.assertTrue(armor.bonus(bonus,stack,false,p)==0,"Broken equipment must disable bonuses");p.setItemSlot(armor.slot,ItemStack.EMPTY);
        }h.succeed();
    }
    @GameTest(template="empty") public static void typedSuitArmorAbsorbsDamageAndUsesDurability(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var stack=TGEquipment.ARMOR.get("t1_combat_chestplate").get().getDefaultInstance();p.setItemSlot(EquipmentSlot.CHEST,stack);
        var source=new techguns.damagesystem.TGDamageSource(p.damageSources().playerAttack(p));float damage=techguns.damagesystem.DamageSystem.applySuitArmor(p,source,10);
        h.assertTrue(damage>0&&damage<10&&stack.getDamageValue()>0,"Typed protection must reduce a real damage amount and wear the armor");
        source.armorPenetration=10;float penetrated=techguns.damagesystem.DamageSystem.applySuitArmor(p,source,10);h.assertTrue(penetrated>=damage,"Armor penetration reduces suit protection");h.succeed();
    }
    @GameTest(template="empty") public static void emptySteamSuitRefillsChestAndDistributesFuelToOtherParts(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var data=TGExtendedPlayer.get(p);
        for(var id:new String[]{"steam_helmet","steam_chestplate","steam_leggings","steam_boots"}){var a=TGEquipment.ARMOR.get(id).get();p.setItemSlot(a.slot,a.getDefaultInstance());}
        data.tg_inventory.setItem(7,new ItemStack(TGItems.COMPRESSED_AIR_TANK.get()));EquipmentEvents.distributePower(p);
        int total=0;for(var stack:p.getArmorSlots()){var a=(TGSuitArmor)stack.getItem();h.assertTrue(a.power(stack)>0,"Every compatible armor part receives power from chest");total+=a.power(stack);}
        h.assertTrue(total==3600,"Distribution must conserve one source tank of 3600 units");
        h.assertTrue(techguns.util.InventoryUtil.countItemInInv(data.tg_inventory.inventory,TGItems.COMPRESSED_AIR_TANK_EMPTY.get().getDefaultInstance(),7,15)==1,"Refilling returns exactly one empty tank");h.succeed();
    }
    @GameTest(template="empty") public static void scubaNeedsMaskAndConsumesOneAirUnit(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var data=TGExtendedPlayer.get(p);var tank=TGAccessories.SCUBA_TANKS.get().getDefaultInstance();data.tg_inventory.setItem(1,tank);p.setAirSupply(260);
        TGAccessories.SCUBA_TANKS.get().onPlayerTick(tank,p);h.assertTrue(p.getAirSupply()==260&&tank.getDamageValue()==0,"A tank without an oxygen mask cannot supply air");
        data.tg_inventory.setItem(0,TGAccessories.TACTICAL_MASK.get().getDefaultInstance());TGAccessories.SCUBA_TANKS.get().onPlayerTick(tank,p);
        h.assertTrue(p.getAirSupply()==280&&tank.getDamageValue()==1,"Mask and tank add twenty air and consume one tank unit");h.succeed();
    }
    @GameTest(template="empty") public static void gasMaskRemovesPoisonAndWitherAndTracksFilterUse(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var mask=TGAccessories.GAS_MASK.get().getDefaultInstance();p.addEffect(new MobEffectInstance(MobEffects.POISON,100));p.addEffect(new MobEffectInstance(MobEffects.WITHER,100));
        TGAccessories.GAS_MASK.get().onPlayerTick(mask,p);h.assertTrue(!p.hasEffect(MobEffects.POISON)&&!p.hasEffect(MobEffects.WITHER)&&mask.getDamageValue()==2,"Each removed status consumes one filter unit");h.succeed();
    }
    @GameTest(template="empty") public static void nightVisionBatteryRefillDoesNotConsumePowerInSameTick(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var data=TGExtendedPlayer.get(p);data.enableNightVision=true;var goggles=TGAccessories.NIGHTVISION_GOGGLES.get().getDefaultInstance();goggles.setDamageValue(goggles.getMaxDamage());data.tg_inventory.setItem(7,TGItems.REDSTONE_BATTERY.get().getDefaultInstance());
        TGAccessories.NIGHTVISION_GOGGLES.get().onPlayerTick(goggles,p);h.assertTrue(goggles.getDamageValue()==0,"Original goggles refill instead of draining on the refill tick");
        TGAccessories.NIGHTVISION_GOGGLES.get().onPlayerTick(goggles,p);h.assertTrue(goggles.getDamageValue()==1,"Enabled vision drains one power unit per following tick");data.enableNightVision=false;TGAccessories.NIGHTVISION_GOGGLES.get().onPlayerTick(goggles,p);h.assertTrue(goggles.getDamageValue()==1,"Disabled goggles conserve the battery");h.succeed();
    }
    @GameTest(template="empty") public static void jetpackHeldJumpConsumesFuelAndGliderSynchronizes(GameTestHelper h){
        var p=h.makeMockSurvivalPlayer();var data=TGExtendedPlayer.get(p);data.enableJetpack=true;data.setJumpkeyPressed(true);p.setDeltaMovement(0,0,0);var pack=TGAccessories.JETPACK.get().getDefaultInstance();
        TGAccessories.JETPACK.get().onPlayerTick(pack,p);h.assertTrue(Math.abs(p.getDeltaMovement().y-0.15)<0.00001&&pack.getDamageValue()==1,"Held jump lifts the player and consumes one fuel unit");
        data.setJumpkeyPressed(false);p.setOnGround(false);p.setShiftKeyDown(true);p.setDeltaMovement(0,-0.4,0);AccessoryItem.glide(p);h.assertTrue(data.isGliding&&p.getDeltaMovement().y> -0.4,"Sneaking in air unfolds the glider and slows descent");
        var other=h.makeMockSurvivalPlayer();TGExtendedPlayer.get(other).applySynchronization(data.synchronizationData(false),false);h.assertTrue(TGExtendedPlayer.get(other).isGliding,"Observer synchronization carries unfolded glider state");h.succeed();
    }
    @GameTest(template="empty") public static void repairsUseSourceMaterialsAndPreserveCamouflage(GameTestHelper h){
        var a=TGEquipment.ARMOR.get("t1_combat_chestplate").get();var stack=a.getDefaultInstance();h.assertTrue(a.repairMaterials(stack).isEmpty(),"Undamaged equipment needs no repair materials");stack.setDamageValue(stack.getMaxDamage()-1);var mats=a.repairMaterials(stack);h.assertTrue(mats.size()==2&&mats.stream().mapToInt(ItemStack::getCount).sum()==4,"Full chestplate repair uses original four ingredient units");
        var scout=TGEquipment.ARMOR.get("t1_scout_helmet").get();var camo=scout.getDefaultInstance();scout.switchCamo(camo);var copy=ItemStack.of(camo.save(new net.minecraft.nbt.CompoundTag()));h.assertTrue(scout.getCurrentCamoIndex(copy)==1,"Armor camouflage survives item persistence");h.succeed();
    }
}
