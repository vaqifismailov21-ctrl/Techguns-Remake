package techguns.entities.spawn;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
/** Reserves the normal monster slot; the join event replaces it using the source danger table. */
public final class TGDummySpawn extends Monster {
    public TGDummySpawn(EntityType<? extends TGDummySpawn> type,Level level){super(type,level);}
}
