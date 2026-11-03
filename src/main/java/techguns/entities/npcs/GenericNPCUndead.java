package techguns.entities.npcs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
public class GenericNPCUndead extends GenericNPC {
    public GenericNPCUndead(EntityType<? extends GenericNPC> type,Level level){super(type,level);}
    @Override public MobType getMobType(){return MobType.UNDEAD;}
    @Override public void aiStep(){
        if(!level.isClientSide&&level.isDay()&&shouldBurnInDay()){
            float brightness=getLightLevelDependentMagicValue();
            if(brightness>.5f&&random.nextFloat()*30<(brightness-.4f)*2&&level.canSeeSky(new BlockPos(getX(),getEyeY(),getZ())))setSecondsOnFire(8);
        }
        super.aiStep();
    }
    @Override protected boolean shouldBurnInDay(){return !getPersistentData().contains("TGSpawner");}
}
