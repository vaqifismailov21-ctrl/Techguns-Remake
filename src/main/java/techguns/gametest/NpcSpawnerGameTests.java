package techguns.gametest;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import techguns.*;
import techguns.entities.npcs.*;
import techguns.entities.spawn.*;
import techguns.tileentities.NpcSpawnerEntity;

@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class NpcSpawnerGameTests {
    @GameTest(template="weapon_arena",batch="npc-spawners") public static void finiteGuardsRespectActiveLimitDespawnAndActualKills(GameTestHelper h){
        var pos=new BlockPos(24,10,24);h.setBlock(pos.below(),Blocks.STONE);h.setBlock(pos,TGWorld.SPAWNER_HOLE.get());
        var spawner=(NpcSpawnerEntity)h.getLevel().getBlockEntity(h.absolutePos(pos));spawner.setParams(2,1,1,0);spawner.addMobType(TGNpcs.ZOMBIESOLDIER.get(),1);
        spawner.setWeaponOverride(TGuns.pistol.get().getDefaultInstance());spawner.tick();
        h.assertTrue(spawner.activeCount()==1&&spawner.remaining()==2,"Spawning a guard must not spend the finite kill budget");
        var first=spawner.activeMobs().iterator().next();h.assertTrue(first.getMainHandItem().is(TGuns.pistol.get()),"Structure weapon override must be copied onto the initialized NPC");
        h.assertTrue(first.getPersistentData().contains("TGSpawner"),"Guard must save its originating spawner");
        spawner.tick();h.assertTrue(spawner.activeCount()==1,"Original max-active limit must stop extra guards");
        first.discard();spawner.tick();h.assertTrue(spawner.remaining()==2&&spawner.activeCount()==1,"Despawn frees a slot and must not count as a kill");
        var second=spawner.activeMobs().iterator().next();second.hurt(h.getLevel().damageSources().generic(),100000);h.assertTrue(spawner.remaining()==1,"Actual guard death decrements finite budget exactly once");spawner.tick();
        h.assertTrue(spawner.activeCount()==1&&spawner.remaining()==1,"Only one final guard remains");var last=spawner.activeMobs().iterator().next();last.hurt(h.getLevel().damageSources().generic(),100000);spawner.tick();
        h.assertTrue(spawner.remaining()==0&&h.getLevel().getBlockState(h.absolutePos(pos)).isAir(),"Cleared source spawner disappears when its finite guards are defeated");h.succeed();
    }
    @GameTest(template="weapon_arena",batch="npc-spawners") public static void savedSpawnerAndNpcRelinkWithoutResettingKillBudget(GameTestHelper h){
        var pos=new BlockPos(24,10,24);h.setBlock(pos.below(),Blocks.STONE);h.setBlock(pos,TGWorld.SPAWNER_SOLDIER.get());
        var spawner=(NpcSpawnerEntity)h.getLevel().getBlockEntity(h.absolutePos(pos));spawner.setParams(5,2,3,0);spawner.setSpawnHeightOffset(2);spawner.addMobType(TGNpcs.ARMYSOLDIER.get(),7);spawner.setWeaponOverride(TGuns.revolver.get().getDefaultInstance());for(int i=0;i<3;i++)spawner.tick();
        var guard=spawner.activeMobs().iterator().next();guard.setNoAi(true);var saved=spawner.saveWithoutMetadata();var guardSaved=guard.saveWithoutId(new CompoundTag());
        h.assertTrue(Math.abs(guard.getY()-(h.absolutePos(pos).getY()+3))<.001,"Original spawn height offset");
        spawner.load(saved);h.assertTrue(spawner.remaining()==5&&spawner.activeCount()==0&&spawner.delay()==3,"Spawner reload retains budget/timer; live guards relink separately");
        guard.discard(); // Simulate unloading the old entity before loading its saved UUID.
        var loaded=TGNpcs.ARMYSOLDIER.get().create(h.getLevel());loaded.load(guardSaved);h.assertTrue(h.getLevel().addFreshEntity(loaded),"Saved guard must actually re-enter the level without a duplicate UUID");
        var linked=techguns.events.NpcSpawnerLinkEvents.spawner(loaded);h.assertTrue(linked==spawner,"Reloaded guard resolves original spawner position and dimension");spawner.relink(loaded);
        h.assertTrue(spawner.activeCount()==1&&loaded.getMainHandItem().is(TGuns.revolver.get()),"Guard and weapon survive reload");
        loaded.hurt(h.getLevel().damageSources().generic(),100000);h.assertTrue(spawner.remaining()==4,"Relinked guard counts once on actual death");guard.discard();h.succeed();
    }
    @GameTest(template="empty",batch="npc-spawners") public static void originalDangerBucketsSelectOnlyEligibleNaturalMobs(GameTestHelper h){
        h.assertTrue(TGSpawnManager.distanceDanger(499,false)==0&&TGSpawnManager.distanceDanger(500,false)==1&&TGSpawnManager.distanceDanger(1000,false)==2&&TGSpawnManager.distanceDanger(2500,false)==3,"Original Overworld danger thresholds");
        h.assertTrue(TGSpawnManager.distanceDanger(249,true)==0&&TGSpawnManager.distanceDanger(250,true)==1&&TGSpawnManager.distanceDanger(1250,true)==3,"Nether distance thresholds are halved");
        var random=net.minecraft.util.RandomSource.create(1729);var near=new HashSet<EntityType<?>>();var far=new HashSet<EntityType<?>>();
        for(int i=0;i<2000;i++){near.add(TGSpawnManager.choose(false,0,random));far.add(TGSpawnManager.choose(false,3,random));h.assertTrue(Set.of(TGNpcs.ZOMBIEPIGMANSOLDIER.get(),TGNpcs.CYBERDEMON.get()).contains(TGSpawnManager.choose(true,3,random)),"Nether spawn table must stay separate");}
        h.assertTrue(near.equals(Set.of(TGNpcs.ZOMBIEFARMER.get(),TGNpcs.ZOMBIEMINER.get())),"Low-danger Overworld uses source farmer/miner bucket");
        h.assertTrue(far.size()==6&&far.contains(TGNpcs.BANDIT.get())&&far.contains(TGNpcs.PSYCHOSTEVE.get()),"High-danger Overworld unlocks all six configured source types");h.succeed();
    }
    private NpcSpawnerGameTests(){}
}
