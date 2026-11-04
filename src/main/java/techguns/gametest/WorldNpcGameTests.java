package techguns.gametest;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.entities.npcs.*;
import techguns.entities.projectiles.*;
import techguns.items.guns.GenericGun;
import techguns.world.*;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class WorldNpcGameTests {
    @GameTest(template="empty",batch="world-npc") public static void originalNpcAttributesEquipmentLootAndSaveRoundTrip(GameTestHelper h){
        var definitions=contract();int count=0;
        for(var element:definitions){var expected=element.getAsJsonObject();String id=expected.get("id").getAsString();
            var type=TGNpcs.TYPES.get(id).get();var npc=type.create(h.getLevel());
            for(var a:expected.getAsJsonArray("attributes")){var value=a.getAsJsonObject();String name=value.get("name").getAsString();var attribute=switch(name){case "MAX_HEALTH"->Attributes.MAX_HEALTH;case "MOVEMENT_SPEED"->Attributes.MOVEMENT_SPEED;case "ATTACK_DAMAGE"->Attributes.ATTACK_DAMAGE;case "FOLLOW_RANGE"->Attributes.FOLLOW_RANGE;case "ARMOR_TOUGHNESS"->Attributes.ARMOR_TOUGHNESS;default->throw new IllegalArgumentException(name);};
                double original=Double.parseDouble(value.get("value").getAsString().replaceAll("[dDfF]$",""));h.assertTrue(Math.abs(npc.getAttributeBaseValue(attribute)-original)<.000001,id+" "+name+" differs from original");}
            h.assertTrue(npc.fireImmune()==expected.get("fireImmune").getAsBoolean(),id+" fire immunity differs");
            npc.onSpawnByManager(3);h.assertTrue(!npc.getMainHandItem().isEmpty(),id+" must receive original equipment");
            var saved=npc.saveWithoutId(new CompoundTag());var loaded=type.create(h.getLevel());loaded.load(saved);
            for(var slot:EquipmentSlot.values())h.assertTrue(ItemStack.matches(npc.getItemBySlot(slot),loaded.getItemBySlot(slot)),id+" equipment/camo changed after reload: "+slot);
            var loot=h.getLevel().getServer().getLootData().getLootTable(npc.getLootTable());h.assertTrue(loot!=LootTable.EMPTY,id+" original loot table must be loaded");
            var egg=(SpawnEggItem)TGNpcs.EGGS.get(id).get();h.assertTrue(egg.getType(null)==type,id+" egg creates wrong type");
            count++;
        }
        h.assertTrue(count==17,"Every migrated humanoid NPC must be checked");Techguns.LOGGER.info("TECHGUNS_NPC_SOURCE_PASSED: {} original attributes, equipment/camo save round-trips, spawn eggs and loaded loot tables",count);h.succeed();
    }
    @GameTest(template="weapon_arena",batch="world-npc",timeoutTicks=300) public static void npcWeaponsUseRealProjectilesAndInfiniteAmmoWithoutTerrainDamage(GameTestHelper h){
        for(int x=22;x<27;x++)for(int z=19;z<27;z++)h.setBlock(x,10,z,Blocks.STONE);
        var choices=new LinkedHashSet<String>();for(var e:contract())for(var choice:e.getAsJsonObject().getAsJsonArray("weaponChoices"))choices.add(choice.getAsString());
        var shooter=TGNpcs.ARMYSOLDIER.get().create(h.getLevel());shooter.setPos(h.absoluteVec(new Vec3(24.5,11,21)));shooter.setYHeadRot(0);shooter.setXRot(0);shooter.setNoAi(true);h.getLevel().addFreshEntity(shooter);
        int count=0;
        for(String id:choices){
            var gun=TGuns.guns().stream().filter(g->g.getId().getPath().equals(id)).findFirst().orElseThrow().get();var stack=gun.getDefaultInstance();shooter.setItemSlot(EquipmentSlot.MAINHAND,stack);int ammo=gun.getCurrentAmmo(stack);
            var target=h.spawn(EntityType.IRON_GOLEM,new Vec3(24.5,11,24));target.setNoAi(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20000);target.setHealth(20000);float before=target.getHealth();
            gun.fireWeaponFromNPC(shooter,.8f,1.15f);
            var shots=h.getLevel().getEntitiesOfClass(GenericProjectile.class,shooter.getBoundingBox().inflate(20),p->p.getShooter()==shooter&&!p.isRemoved());h.assertTrue(!shots.isEmpty(),"NPC weapon did not create a projectile: "+id);
            for(var shot:shots){
                var saved=shot.saveWithoutId(new CompoundTag());h.assertTrue(!saved.getBoolean("blockdamage"),"NPC shots must preserve original terrain-safe behavior: "+id);
                if(shot instanceof EnergyProjectile beam&&beam.isBeam()){if(beam.mode==EnergyProjectile.Mode.NDR)beam.tick();}
                else {shot.setPos(h.absoluteVec(new Vec3(24.5,12.3,22)));shot.setDeltaMovement(0,0,3);shot.tick();}
            }
            h.assertTrue(before>target.getHealth(),"NPC weapon failed to hurt real target: "+id);h.assertTrue(gun.getCurrentAmmo(stack)==ammo,"NPC ammunition must not be consumed: "+id);
            shots.forEach(Entity::discard);target.discard();count++;
        }
        shooter.discard();Techguns.LOGGER.info("TECHGUNS_NPC_WEAPON_MATRIX_PASSED: {} original NPC weapon choices hit living targets, preserve ammunition and cannot destroy terrain",count);h.succeed();
    }
    @GameTest(template="empty",batch="world-npc") public static void factionsAndDifficultyKeepOriginalRules(GameTestHelper h){
        var hostile=techguns.api.npc.factions.TGNpcFaction.HOSTILE;var turret=techguns.api.npc.factions.TGNpcFaction.TURRET;var neutral=techguns.api.npc.factions.TGNpcFaction.NEUTRAL;
        h.assertTrue(!techguns.factions.TGNpcFactions.isHostile(hostile,hostile),"Hostile NPCs do not retaliate against their own faction");
        h.assertTrue(techguns.factions.TGNpcFactions.isHostile(hostile,turret)&&techguns.factions.TGNpcFactions.isHostile(hostile,neutral),"Hostiles oppose turret/neutral factions");
        h.assertTrue(!techguns.factions.TGNpcFactions.isHostile(turret,neutral),"Turrets and neutrals are not inherently hostile");
        var pistol=TGuns.pistol.get();h.assertTrue(pistol.aiAttackRange==24&&pistol.aiAttackTime==30&&pistol.aiBurstCount==3&&pistol.aiBurstAttackTime==10,"Pistol AI must retain source cadence independently of player firing cadence");
        var ak=TGuns.ak47.get();h.assertTrue(ak.aiAttackRange==24&&ak.aiAttackTime==30&&ak.aiBurstCount==3&&ak.aiBurstAttackTime==3,"AK source burst timing");h.succeed();
    }
    @GameTest(template="weapon_arena",batch="world-npc") public static void originalOreVeinsRespectStoneAndConfigSwitches(GameTestHelper h){
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(12,12,12));
        for(int x=8;x<32;x++)for(int y=8;y<17;y++)for(int z=8;z<32;z++)h.setBlock(x,y,z,Blocks.STONE);
        var wall=center.offset(8,0,8);level.setBlockAndUpdate(wall,Blocks.DEEPSLATE.defaultBlockState());
        int placed=LegacyOreFeature.vein(level,RandomSource.create(1729),center,TGWorld.ORES.get(OreKind.COPPER).get().defaultBlockState(),7);
        h.assertTrue(placed>0,"Actual legacy ore vein must replace stone");h.assertTrue(level.getBlockState(wall).is(Blocks.DEEPSLATE),"Original stone predicate must leave modern deepslate unchanged");
        boolean[] old={TGConfig.doOreGenCopper,TGConfig.doOreGenTin,TGConfig.doOreGenLead,TGConfig.doOreGenUranium,TGConfig.doOreGenTitanium};
        try{TGConfig.doOreGenCopper=TGConfig.doOreGenTin=TGConfig.doOreGenLead=TGConfig.doOreGenUranium=TGConfig.doOreGenTitanium=false;h.assertTrue(LegacyOreFeature.generate(level,RandomSource.create(1),center)==0,"Disabled original ore switches must stop every attempt");}
        finally{TGConfig.doOreGenCopper=old[0];TGConfig.doOreGenTin=old[1];TGConfig.doOreGenLead=old[2];TGConfig.doOreGenUranium=old[3];TGConfig.doOreGenTitanium=old[4];}
        h.assertTrue(OreKind.COPPER.attempts==12&&OreKind.COPPER.minY==5&&OreKind.COPPER.maxY==80,"Original copper distribution");
        h.assertTrue(OreKind.URANIUM.attempts==4&&OreKind.URANIUM.minY==4&&OreKind.URANIUM.maxY==24,"Original uranium distribution");
        h.assertTrue(level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).containsKey(net.minecraft.resources.ResourceLocation.parse("techguns:legacy_ores")),"Ore feature must be present in the actual world generation registry");
        h.succeed();
    }
    @GameTest(template="empty",batch="world-npc") public static void clustersRetainSourceWeightsConfigAndUnbreakableBlocks(GameTestHelper h){
        var common=TGOreClusters.entries(ClusterKind.COMMON_METAL);h.assertTrue(common.stream().mapToInt(TGOreClusters.Entry::weight).sum()==100,"Original common metal weights sum to 100");
        var coal=TGOreClusters.entries(ClusterKind.COAL);h.assertTrue(coal.get(0).weight()==99&&coal.get(1).weight()==1&&coal.get(1).item().is(Items.DIAMOND),"Original coal cluster has 1/100 diamond output");
        for(var kind:ClusterKind.values()){var block=TGWorld.CLUSTERS.get(kind).get();h.assertTrue(block.defaultBlockState().getDestroySpeed(h.getLevel(),h.absolutePos(BlockPos.ZERO))<0,"Cluster must be unbreakable: "+kind);h.assertTrue(kind.amountMultiplier()>0&&kind.powerMultiplier()>0,"Cluster configuration loaded: "+kind);}
        var first=TGOreClusters.choose(ClusterKind.COMMON_METAL,RandomSource.create(123));first.item().setCount(20);var second=TGOreClusters.choose(ClusterKind.COMMON_METAL,RandomSource.create(123));h.assertTrue(second.item().getCount()==1,"Cluster output must not share mutable stacks");
        h.assertTrue(TGOreClusters.powerPerTick(100,2,.2)==192,"Original cluster power formula");h.succeed();
    }
    private static JsonArray contract(){try(var stream=WorldNpcGameTests.class.getResourceAsStream("/data/techguns/verification/npc-source-contract.json");var reader=new java.io.InputStreamReader(Objects.requireNonNull(stream),java.nio.charset.StandardCharsets.UTF_8)){return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("npcs");}catch(java.io.IOException error){throw new IllegalStateException(error);}}
    private WorldNpcGameTests(){}
}
