package techguns.power.wire;

import net.minecraft.core.BlockPos;

/** A point a wire hangs on: the block holding it (its main block) and the number of the point on that block. */
public record WireNode(BlockPos pos,int point){
    public WireNode{pos=pos.immutable();}
}
