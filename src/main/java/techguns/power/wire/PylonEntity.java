package techguns.power.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Base of a pylon; only there for its renderer (the tower and arms reach far out of the block). */
public final class PylonEntity extends BlockEntity {
    public PylonEntity(BlockPos pos,BlockState state){super(WireBlocks.PYLON_ENTITY.get(),pos,state);}
    @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(2.5,0,2.5).expandTowards(0,PylonBlock.HEIGHT,0);}
}
