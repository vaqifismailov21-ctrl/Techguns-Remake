package techguns.entities.npcs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
public class GenericNPCUndead extends GenericNPC {
    public GenericNPCUndead(EntityType<? extends GenericNPC> type,Level level){super(type,level);}
    /** 1.21.1: creature type is the entity tag minecraft:undead (data/minecraft/tags/entity_type/undead.json). */
    @Override public boolean isInvertedHealAndHarm(){return true;}
    @Override public void aiStep(){
        if(!level().isClientSide&&level().isDay()&&shouldBurnInDay()){
            float brightness=getLightLevelDependentMagicValue();
            if(brightness>.5f&&random.nextFloat()*30<(brightness-.4f)*2&&level().canSeeSky(BlockPos.containing(getX(),getEyeY(),getZ())))igniteForSeconds(8);
        }
        super.aiStep();
    }
    @Override protected boolean shouldBurnInDay(){return !getPersistentData().contains("TGSpawner");}
}
