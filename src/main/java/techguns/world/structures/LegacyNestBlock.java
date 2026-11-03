package techguns.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import techguns.TGWorld;

/** Only the original nest palette; metadata is zero for each of these source blocks. */
public class LegacyNestBlock {
    public static final LegacyNestBlock AIR=new LegacyNestBlock(Blocks.AIR,0);
    private final Block block;
    public LegacyNestBlock(Block block,int metadata){this.block=block;}
    public void setBlock(LegacyStructurePlan world,BlockPos pos,int rotation){world.setBlockState(pos,block.defaultBlockState(),2);}
    public static LegacyNestBlock weighted(Block[] blocks,int[] weights){return new LegacyNestBlock(Blocks.AIR,0){
        @Override public void setBlock(LegacyStructurePlan world,BlockPos pos,int rotation){int total=0;for(int weight:weights)total+=weight;int roll=world.random.nextInt(total+1),sum=0;for(int i=0;i<weights.length;i++){sum+=weights[i];if(roll<=sum){world.setBlockState(pos,blocks[i].defaultBlockState(),2);return;}}throw new IllegalStateException("Source nest palette weights");}
    };}
    public static LegacyNestBlock spawner(){return new LegacyNestBlock(TGWorld.SPAWNER_HOLE.get(),0){
        @Override public void setBlock(LegacyStructurePlan world,BlockPos pos,int rotation){super.setBlock(world,pos,rotation);world.setBlockData(pos,LegacyStructurePlan.guardData(7,3,150,1,java.util.Map.of("techguns:alienbug",1)));}
    };}
}
