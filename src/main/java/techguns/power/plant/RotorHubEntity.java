package techguns.power.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Part entity of the rotor hub; its renderer draws the blades hung on it, which reach far out of the block. */
public final class RotorHubEntity extends PlantPartEntity {
    public RotorHubEntity(BlockPos pos,BlockState state){super(PlantBlocks.HUB_ENTITY.get(),pos,state);}
    @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(WindPlant.ROTOR+1);}
}
