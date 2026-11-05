package techguns.world.dungeon;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import techguns.TGWorld;
import techguns.world.structures.LegacyStructurePlan;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;

public final class DungeonWorld extends LegacyStructurePlan {
    public final RandomSource rand;
    public DungeonWorld(RandomSource random,Function<BlockPos,BlockState> terrain){super(random,terrain);rand=random;}
    public DungeonSpawner getTileEntity(BlockPos pos){return getBlockState(pos).getBlock() instanceof techguns.blocks.NpcSpawnerBlock?new DungeonSpawner(this,pos):null;}
    public void resolveLegacyShapes(){
        for(var pos:new java.util.ArrayList<>(blocks().keySet())){var lower=getBlockState(pos);if(lower.getBlock() instanceof DoorBlock&&lower.getValue(DoorBlock.HALF)==DoubleBlockHalf.LOWER){var top=pos.above();var upper=getBlockState(top);if(upper.is(lower.getBlock())&&upper.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER){var merged=lower.setValue(DoorBlock.HINGE,upper.getValue(DoorBlock.HINGE)).setValue(DoorBlock.POWERED,upper.getValue(DoorBlock.POWERED));setBlockState(pos,merged,2);setBlockState(top,merged.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),2);}}}
        for(var pos:new java.util.ArrayList<>(blocks().keySet())){var first=getBlockState(pos);if(!(first.getBlock() instanceof ChestBlock)||first.getValue(ChestBlock.TYPE)!=ChestType.SINGLE)continue;var other=pos.relative(first.getValue(ChestBlock.FACING).getClockWise());var second=getBlockState(other);if(second.is(first.getBlock())&&second.getValue(ChestBlock.FACING)==first.getValue(ChestBlock.FACING)&&second.getValue(ChestBlock.TYPE)==ChestType.SINGLE){var a=blockData().get(pos);var b=blockData().get(other);setBlockState(pos,first.setValue(ChestBlock.TYPE,ChestType.LEFT),2);setBlockState(other,second.setValue(ChestBlock.TYPE,ChestType.RIGHT),2);if(a!=null)setBlockData(pos,a);if(b!=null)setBlockData(other,b);}}
    }
}
