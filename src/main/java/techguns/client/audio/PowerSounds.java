package techguns.client.audio;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import techguns.TGSounds;
import techguns.power.*;

/** Client side: running loops of the generators and the wind turbine, the floodlight switch, the full cell beep and the reactor alarm. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=techguns.Techguns.MODID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class PowerSounds {
    /** Nothing of one world is kept for the next. */
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void leave(net.minecraftforge.event.level.LevelEvent.Unload event){
        if(event.getLevel().isClientSide()){for(var loop:loops.values())Minecraft.getInstance().getSoundManager().stop(loop);loops.clear();last.clear();}
    }
    private static final Map<BlockPos,Loop> loops=new HashMap<>();
    private static final Map<BlockPos,Integer> last=new HashMap<>();

    /** A looping sound that stays while its block keeps working. */
    private static final class Loop extends AbstractTickableSoundInstance {
        private final BlockEntity entity;
        private Loop(BlockEntity entity,SoundEvent event,float volume,float pitch){
            super(event,SoundSource.BLOCKS,RandomSource.create());this.entity=entity;
            var pos=entity.getBlockPos();x=pos.getX()+.5;y=pos.getY()+.5;z=pos.getZ()+.5;
            if(entity instanceof techguns.power.plant.PlantController plant&&plant.formed){
                // from the middle of the plant; the wind turbine sounds from its rotor, high up the tower
                float[] c=plant instanceof techguns.power.plant.WindPlant w?techguns.power.plant.PlantModel.rotorCentre(Math.max(1,w.towers)):switch(plant.kind){case FUSION->new float[]{0,0.5F,2};case WIND->new float[]{0,0.5F,0};default->new float[]{0,0.5F,1};};
                var f=plant.facing();var r=f.getClockWise();var b=f.getOpposite();
                x=pos.getX()+.5+r.getStepX()*c[0]+b.getStepX()*c[2];y=pos.getY()+c[1];z=pos.getZ()+.5+r.getStepZ()*c[0]+b.getStepZ()*c[2];
            }
            looping=true;delay=0;this.volume=volume;this.pitch=pitch;attenuation=SoundInstance.Attenuation.LINEAR;
        }
        @Override public void tick(){
            if(entity.isRemoved()||!running(entity)){stop();loops.remove(entity.getBlockPos(),this);return;}
            if(entity instanceof techguns.power.plant.WindPlant wind){float k=wind.rate/(float)techguns.power.plant.WindPlant.RATE;volume=1.4F+1.2F*k;pitch=0.75F+0.35F*k;}
            if(entity instanceof WindTurbineEntity){int rate=windRate(entity);volume=0.35F+0.5F*rate/PowerBlocks.WIND_RATE;pitch=0.8F+0.4F*rate/PowerBlocks.WIND_RATE;}
        }
    }
    private static int windRate(BlockEntity entity){var base=WindTurbineEntity.findBase(entity.getLevel(),entity.getBlockPos());return base==null?0:base.rate;}
    private static boolean lit(BlockEntity entity){var state=entity.getBlockState();return state.hasProperty(BlockStateProperties.LIT)&&state.getValue(BlockStateProperties.LIT);}
    private static boolean running(BlockEntity entity){
        if(entity instanceof WindTurbineEntity)return entity.getBlockState().getValue(WindTurbineBlock.PART)==WindTurbineBlock.Part.HEAD&&windRate(entity)>0;
        if(entity instanceof FloodlightEntity)return false;
        if(entity instanceof SolarPanelEntity panel)return panel.currentRate()>0;
        return lit(entity);
    }
    private static SoundEvent loopSound(BlockEntity entity){
        if(entity instanceof SolarPanelEntity||entity instanceof FuelGeneratorEntity||entity instanceof LiquidGeneratorEntity)return null;
        if(entity instanceof GeothermalGeneratorEntity)return TGSounds.GEOTHERMAL_GENERATOR_RUN.get();
        if(entity instanceof NuclearGeneratorEntity)return TGSounds.NUCLEAR_GENERATOR_RUN.get();
        if(entity instanceof WindTurbineEntity)return TGSounds.WIND_TURBINE_RUN.get();
        if(entity instanceof techguns.power.plant.PlantController plant)return switch(plant.kind){case WIND->TGSounds.PLANT_WIND_RUN.get();case GEOTHERMAL->TGSounds.PLANT_GEOTHERMAL_RUN.get();case FISSION->TGSounds.PLANT_FISSION_RUN.get();case FUSION->TGSounds.PLANT_FUSION_RUN.get();};
        return null;
    }
    private static void once(BlockEntity entity,SoundEvent event,float volume){var pos=entity.getBlockPos();Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(event,SoundSource.BLOCKS,volume,1,RandomSource.create(),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5));}

    /** Called every client tick by each power block entity. */
    public static void tick(BlockEntity entity){
        var level=entity.getLevel();if(level==null||!level.isClientSide)return;
        var pos=entity.getBlockPos();var state=entity.getBlockState();
        if(entity instanceof FloodlightEntity){int now=lit(entity)?1:0;Integer was=last.put(pos,now);if(was!=null&&was==0&&now==1)once(entity,TGSounds.FLOODLIGHT_ON.get(),0.6F);return;}
        if(entity instanceof EnergyCellEntity){int now=state.getValue(EnergyCellBlock.CHARGE);Integer was=last.put(pos,now);if(was!=null&&was<4&&now==4)once(entity,TGSounds.ENERGY_CELL_FULL.get(),0.5F);return;}
        effects(entity,state,level.getGameTime());
        if(entity instanceof techguns.power.plant.FissionReactor reactor&&reactor.overheated&&level.getGameTime()%40==0)once(entity,TGSounds.REACTION_CHAMBER_WARNING.get(),1F);
        if(entity instanceof NuclearGeneratorEntity reactor&&reactor.overheated&&level.getGameTime()%40==0)once(entity,TGSounds.REACTION_CHAMBER_WARNING.get(),0.8F);
        if(entity instanceof WindTurbineEntity&&level.getGameTime()%10!=Math.floorMod(pos.asLong(),10L))return;
        var sound=loopSound(entity);if(sound==null)return;
        var loop=loops.get(pos);
        if(running(entity)&&(loop==null||loop.isStopped()||!Minecraft.getInstance().getSoundManager().isActive(loop))){
            if(loop!=null)Minecraft.getInstance().getSoundManager().stop(loop);
            var next=new Loop(entity,sound,entity instanceof WindTurbineEntity?0.6F:0.55F,0.95F+level.random.nextFloat()*0.1F);
            loops.put(pos,next);Minecraft.getInstance().getSoundManager().play(next);
        }
    }
    /** Exhaust smoke of the burning generators and steam over the geothermal generator. */
    private static void effects(BlockEntity entity,net.minecraft.world.level.block.state.BlockState state,long time){
        var level=entity.getLevel();var pos=entity.getBlockPos();var random=level.random;
        if(entity instanceof SolarPanelEntity panel){
            // sun glints on the cells and a rare spark at the junction box while it makes power
            if(random.nextInt(160)==0&&panel.currentRate()>0)level.addParticle(net.minecraft.core.particles.ParticleTypes.WAX_OFF,pos.getX()+random.nextDouble(),pos.getY()+0.45,pos.getZ()+random.nextDouble(),0,0.01,0);
            if(random.nextInt(900)==0&&panel.currentRate()>0)level.addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,pos.getX()+0.5,pos.getY()+0.3,pos.getZ()+0.5,random.nextGaussian()*0.05,0.05,random.nextGaussian()*0.05);
            return;
        }
        if(!lit(entity))return;
        if(entity instanceof FuelGeneratorEntity||entity instanceof LiquidGeneratorEntity){
            boolean liquid=entity instanceof LiquidGeneratorEntity;var facing=state.getValue(GeneratorBlock.FACING);
            // the coal generator sounds like its fire box, the liquid generator like the lava it burns
            float x=pos.getX()+.5F,y=pos.getY()+.5F,z=pos.getZ()+.5F;
            if(!liquid){
                if(random.nextInt(12)==0)sound(level,x,y,z,net.minecraft.sounds.SoundEvents.FURNACE_FIRE_CRACKLE,0.7F,0.9F+random.nextFloat()*0.2F);
                if(random.nextInt(30)==0)sound(level,x,y,z,net.minecraft.sounds.SoundEvents.BLASTFURNACE_FIRE_CRACKLE,0.5F,0.9F+random.nextFloat()*0.2F);
                if(random.nextInt(60)==0)sound(level,x,y,z,net.minecraft.sounds.SoundEvents.FIRE_AMBIENT,0.9F,0.8F+random.nextFloat()*0.2F);
            }else{
                if(random.nextInt(10)==0)sound(level,x,y,z,net.minecraft.sounds.SoundEvents.LAVA_POP,0.5F,0.9F+random.nextFloat()*0.2F);
                if(random.nextInt(50)==0)sound(level,x,y,z,net.minecraft.sounds.SoundEvents.LAVA_AMBIENT,0.6F,0.9F+random.nextFloat()*0.15F);
                if(random.nextInt(200)==0)sound(level,x,y,z,net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,0.12F,1.6F+random.nextFloat()*0.3F);
            }
            var top=liquid?FacingMachineBlock.local(pos,facing,3.5,18.2,11.5):FacingMachineBlock.local(pos,facing,11.5,21.7,10.5);
            var smoke=net.minecraft.core.particles.ParticleTypes.SMOKE;
            // exhaust: a steady stream with a puff on every few strokes, a bigger cloud now and then
            if(time%2==0)level.addParticle(smoke,top.x+(random.nextDouble()-.5)*.08,top.y,top.z+(random.nextDouble()-.5)*.08,(random.nextDouble()-.5)*.01,0.06+random.nextDouble()*0.03,(random.nextDouble()-.5)*.01);
            if(time%(liquid?9:6)==0)for(int i=0;i<2;i++)level.addParticle(smoke,top.x,top.y+0.02,top.z,(random.nextDouble()-.5)*.03,0.1,(random.nextDouble()-.5)*.03);
            if(random.nextInt(liquid?40:25)==0)level.addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,top.x,top.y+0.05,top.z,0,0.06,0);
            if(liquid&&random.nextInt(120)==0)level.addParticle(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME,top.x,top.y+0.02,top.z,0,0.04,0);
            if(!liquid&&time%5==0){var fire=FacingMachineBlock.local(pos,facing,8,5.5,0.6);level.addParticle(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME,fire.x+(random.nextDouble()-.5)*.3,fire.y+(random.nextDouble()-.5)*.1,fire.z,0,0.006,0);}
            if(!liquid&&random.nextInt(60)==0){var fire=FacingMachineBlock.local(pos,facing,8,5.5,0.6);level.addParticle(net.minecraft.core.particles.ParticleTypes.LAVA,fire.x,fire.y,fire.z,0,0,0);}
        }else if(entity instanceof GeothermalGeneratorEntity&&time%12==0){
            level.addParticle(net.minecraft.core.particles.ParticleTypes.WHITE_ASH,pos.getX()+.5+(random.nextDouble()-.5)*.6,pos.getY()+1.05,pos.getZ()+.5+(random.nextDouble()-.5)*.6,0,0.02,0);
            level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,pos.getX()+.5+(random.nextDouble()-.5)*.4,pos.getY()+1.0,pos.getZ()+.5+(random.nextDouble()-.5)*.4,0,0.02,0);
        }
    }
    private static void sound(net.minecraft.world.level.Level level,float x,float y,float z,SoundEvent event,float volume,float pitch){level.playLocalSound(x,y,z,event,SoundSource.BLOCKS,volume,pitch,false);}
    /** Leaving a world drops the bookkeeping. */
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void leave(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){loops.clear();last.clear();}
    private PowerSounds(){}
}
