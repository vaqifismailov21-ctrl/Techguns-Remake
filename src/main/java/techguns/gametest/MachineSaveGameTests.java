package techguns.gametest;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.TGItems;
import techguns.TGOreDrill;
import techguns.TGTurrets;
import techguns.TGWorld;
import techguns.TGuns;
import techguns.Techguns;
import techguns.oredrill.OreDrillDefinition;
import techguns.oredrill.OreDrillTileEntMaster;
import techguns.oredrill.OreDrillType;
import techguns.tileentities.TurretTileEnt;
import techguns.world.ClusterKind;

/**
 * Chunk saving with a live turret and a formed ore drill: the chunk must stay saved after serialisation (no endless save
 * loop) and two saves must not share mutable NBT objects, which the asynchronous region writer would iterate while the
 * next save changes them.
 */
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class MachineSaveGameTests {
    private static void shared(Tag a,Tag b,String path,IdentityHashMap<Tag,String> seen,List<String> out){
        if(a==b&&(a instanceof CompoundTag||a instanceof CollectionTag<?>)){out.add(path);return;}
        if(a instanceof CompoundTag ca&&b instanceof CompoundTag cb){for(String key:ca.getAllKeys())if(cb.contains(key))shared(ca.get(key),cb.get(key),path+"/"+key,seen,out);}
        else if(a instanceof CollectionTag<?> la&&b instanceof CollectionTag<?> lb){for(int i=0;i<Math.min(la.size(),lb.size());i++)shared(la.get(i),lb.get(i),path+"["+i+"]",seen,out);}
    }

    /** Other tests share the chunk; vanilla spawners keep their SpawnData entity tag by reference, which is not ours to check. */
    private static void onlyTechgunsBlockEntities(CompoundTag chunk){var list=chunk.getList("block_entities",Tag.TAG_COMPOUND);list.removeIf(t->!((CompoundTag)t).getString("id").startsWith(Techguns.MODID+":"));}

    @GameTest(template="empty",timeoutTicks=200)
    public static void machinesSaveWithoutSharedOrRedirtiedChunks(GameTestHelper h){
        var level=h.getLevel();var player=h.makeMockPlayer();
        var turretPos=h.absolutePos(new BlockPos(1,2,1));level.setBlockAndUpdate(turretPos.below(),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(turretPos,TGTurrets.BLOCK.get().defaultBlockState());
        var turret=(TurretTileEnt)level.getBlockEntity(turretPos);turret.setOwner(player);turret.energy.receiveEnergy(3000,false);
        turret.inventory.setStackInSlot(TurretTileEnt.SLOT_WEAPON,TGuns.m4.get().getDefaultInstance());turret.inventory.setStackInSlot(TurretTileEnt.SLOT_ARMOR,new ItemStack(TGItems.TURRET_ARMOR_STEEL.get()));turret.inventory.setStackInSlot(0,TGuns.m4.get().getAmmoType().getAmmo(0)[0].copyWithCount(8));turret.spawnTurret(level,turretPos);
        var drillPos=h.absolutePos(new BlockPos(3,3,3));level.setBlockAndUpdate(drillPos.below(2),TGWorld.CLUSTERS.get(ClusterKind.COAL).get().defaultBlockState());level.setBlockAndUpdate(drillPos.below(),TGOreDrill.BLOCKS.get(OreDrillType.ROD).get().defaultBlockState());level.setBlockAndUpdate(drillPos,TGOreDrill.CONTROLLER.get().defaultBlockState());
        var drill=(OreDrillTileEntMaster)level.getBlockEntity(drillPos);drill.setOwner(player);OreDrillDefinition.form(level,drillPos);drill.inventory.setStackInSlot(0,new ItemStack(TGItems.OREDRILLHEAD_STEEL.get()));drill.inventory.setStackInSlot(1,new ItemStack(net.minecraft.world.item.Items.COAL,16));
        drill.inputTank.fill(new net.minecraftforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA,6000),net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);drill.tick();
        h.runAfterDelay(40,()->{
            var problems=new ArrayList<String>();
            for(var pos:new BlockPos[]{turretPos,drillPos}){
                var chunk=level.getChunkAt(pos);chunk.setUnsaved(false);
                var first=ChunkSerializer.write(level,chunk);
                if(chunk.isUnsaved())problems.add("chunk "+new ChunkPos(pos)+" marked unsaved again by its own serialisation");
                var second=ChunkSerializer.write(level,chunk);onlyTechgunsBlockEntities(first);onlyTechgunsBlockEntities(second);shared(first,second,"chunk",new IdentityHashMap<>(),problems);
            }
            var entity=turret.mountedTurret;h.assertTrue(entity!=null,"Turret entity must exist for the entity save check");
            shared(entity.saveWithoutId(new CompoundTag()),entity.saveWithoutId(new CompoundTag()),"turretEntity",new IdentityHashMap<>(),problems);
            h.assertTrue(problems.isEmpty(),"Unsafe machine save data: "+problems);
            Techguns.LOGGER.info("TECHGUNS_MACHINE_SAVE_PASSED: turret and formed drill chunks stay saved after serialisation and share no mutable NBT between saves");
            h.succeed();
        });
    }
}
