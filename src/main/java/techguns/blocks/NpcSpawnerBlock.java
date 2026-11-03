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
    public final boolean soldier;
    public NpcSpawnerBlock(boolean soldier){super(Properties.copy(Blocks.STONE).strength(-1,2).noOcclusion().noCollission().noLootTable());this.soldier=soldier;}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext context){return Block.box(2,0,2,14,2,14);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new NpcSpawnerEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world,BlockState state,BlockEntityType<T> type){return world.isClientSide?null:createTickerHelper(type,TGWorld.NPC_SPAWNER_ENTITY.get(),(level,pos,block,spawner)->spawner.tick());}
    @Override public void setPlacedBy(Level world,BlockPos pos,BlockState state,LivingEntity entity,ItemStack stack){if(!world.isClientSide&&world.getBlockEntity(pos) instanceof NpcSpawnerEntity spawner)spawner.addMobType((soldier?TGNpcs.ARMYSOLDIER:TGNpcs.ZOMBIESOLDIER).get(),1);}
}
