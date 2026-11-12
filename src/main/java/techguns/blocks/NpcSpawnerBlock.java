package techguns.blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import techguns.*;
import techguns.tileentities.NpcSpawnerEntity;

public final class NpcSpawnerBlock extends BaseEntityBlock {
    /** Menus open from use() with the block position; the vanilla spectator path opened them without it and the client crashed. */
    @Override public net.minecraft.world.MenuProvider getMenuProvider(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos){return null;}
    public final boolean soldier;
    public NpcSpawnerBlock(boolean soldier){super(Properties.copy(Blocks.STONE).strength(-1,2).noOcclusion().noCollission().noLootTable());this.soldier=soldier;}
    /**
     * People (soldiers, bandits, the helicopter) come out of a soldier spawn point; zombies, skeletons, bugs and
     * other monsters crawl out of a hole in the ground. Which look a spawner has follows what it spawns.
     */
    public static final java.util.Set<String> PEOPLE=java.util.Set.of("armysoldier","commando","stormtrooper","bandit","outcast","dictatordave","psychosteve",
        "supermutantbasic","supermutantelite","supermutantheavy","attackhelicopter");
    /** True: soldier spawn point, false: hole, null: nothing to decide by. Weighted by the spawn chances. */
    public static Boolean soldierLook(java.util.Map<String,Integer> mobs){
        int people=0,monsters=0;
        for(var e:mobs.entrySet()){var id=e.getKey();int i=id.indexOf(':');var path=i<0?id:id.substring(i+1);
            if(i>=0&&!id.substring(0,i).equals(Techguns.MODID)){monsters+=e.getValue();continue;}
            if(PEOPLE.contains(path))people+=e.getValue();else monsters+=e.getValue();}
        return people+monsters==0?null:people>=monsters;
    }
    public static Block blockFor(boolean soldier){return (soldier?TGWorld.SPAWNER_SOLDIER:TGWorld.SPAWNER_HOLE).get();}
    /** Spawners already in a world get the look that fits their guards; their state goes over to the new block. */
    private static void matchLook(Level level,BlockPos pos,BlockState state,NpcSpawnerEntity spawner){
        if(spawner.lookChecked)return;
        var want=soldierLook(spawner.mobWeights());if(want==null)return;
        spawner.lookChecked=true;
        if(!(state.getBlock() instanceof NpcSpawnerBlock block)||want==block.soldier)return;
        var tag=spawner.saveWithoutMetadata();
        level.setBlock(pos,blockFor(want).defaultBlockState(),3);
        if(level.getBlockEntity(pos) instanceof NpcSpawnerEntity next){next.load(tag);next.adoptActive(spawner);next.lookChecked=true;next.setChanged();}
    }
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext context){return Block.box(2,0,2,14,2,14);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new NpcSpawnerEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world,BlockState state,BlockEntityType<T> type){return world.isClientSide?null:createTickerHelper(type,TGWorld.NPC_SPAWNER_ENTITY.get(),(level,pos,block,spawner)->{matchLook(level,pos,block,spawner);if(!spawner.isRemoved())spawner.tick();});}
    @Override public void setPlacedBy(Level world,BlockPos pos,BlockState state,LivingEntity entity,ItemStack stack){if(!world.isClientSide&&world.getBlockEntity(pos) instanceof NpcSpawnerEntity spawner){spawner.addMobType((soldier?TGNpcs.ARMYSOLDIER:TGNpcs.ZOMBIESOLDIER).get(),1);spawner.lookChecked=true;}}
}
