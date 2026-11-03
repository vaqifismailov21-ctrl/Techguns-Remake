package techguns.machines;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import techguns.*;
/** Original work sounds and particle timings, emitted only when the server advances an operation. */
public final class MachineWorkEffects {
    private static void sound(ProcessingMachine m,SoundEvent sound,float volume,float pitch){m.getLevel().playSound(null,m.getBlockPos(),sound,SoundSource.BLOCKS,volume,pitch);}
    public static void work(ProcessingMachine m){int progress=m.progress,total=m.totalTime,half=Math.round(total*.5f);var level=(ServerLevel)m.getLevel();var pos=m.getBlockPos();
        switch(m.kind){
            case AMMO_PRESS->{if(progress==Math.round(total*.05f)||progress==Math.round(total*.05f)+half)sound(m,TGSounds.AMMO_PRESS_WORK1.get(),.5f,1);else if(progress==Math.round(total*.3f)||progress==Math.round(total*.3f)+half)sound(m,TGSounds.AMMO_PRESS_WORK2.get(),.5f,1);}
            case METAL_PRESS->{if(progress==Math.round(total*.075f)||progress==Math.round(total*.075f)+half)sound(m,TGSounds.METAL_PRESS_WORK.get(),.5f,1);}
            case CHEM_LAB->{if(progress==1||progress==1+half)sound(m,TGSounds.CHEM_LAB_WORK.get(),.65f,1);if(progress%10==0)level.sendParticles(ParticleTypes.EFFECT,pos.getX()+.5+level.random.nextFloat(),pos.getY()+.5+level.random.nextFloat(),pos.getZ()+.5+level.random.nextFloat(),1,0,.1,0,.1);}
            case FABRICATOR->{if(progress==1)sound(m,TGSounds.FABRICATOR_WORK.get(),.5f,1);}
            case GRINDER->{if(progress==1)sound(m,TGSounds.GRINDER_START.get(),1,1);else if(progress==Math.round(total*.2f)+1||progress==Math.round(total*.6f)-1)sound(m,TGSounds.GRINDER_WORK.get(),1,1);if(progress%2==0){var stack=m.consumed(0);if(stack.getItem() instanceof techguns.items.guns.GenericGun)stack=TGItems.PLATE_STEEL.get().getDefaultInstance();if(!stack.isEmpty())level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,stack),pos.getX()+.5,pos.getY()+.6,pos.getZ()+.5,1,.05,.1,.05,.1);}}
            case BLAST_FURNACE->{if(progress%35==0)sound(m,SoundEvents.FURNACE_FIRE_CRACKLE,.75f,1);if(progress%20==0){sound(m,SoundEvents.FIRE_AMBIENT,.75f,.5f+level.random.nextFloat()*.15f);var facing=m.getBlockState().getValue(MachineBlock.FACING);double x=pos.getX()+.5+facing.getStepX()*.52,z=pos.getZ()+.5+facing.getStepZ()*.52,y=pos.getY()+level.random.nextDouble()*6/16+3/16d;level.sendParticles(ParticleTypes.SMOKE,x,y,z,1,.1,0,.1,0);level.sendParticles(ParticleTypes.FLAME,x,y,z,1,.1,0,.1,0);}}
            case CHARGING_STATION->charge(m);
            default->{}
        }
    }
    public static void charge(ProcessingMachine m){long time=m.getLevel().getGameTime();if(time>=m.nextChargeSound){m.nextChargeSound=time+20;sound(m,TGSounds.CHARGING_STATION_WORK.get(),.35f,1);}var direction=m.getBlockState().getValue(MachineBlock.FACING);for(var side:Direction.Plane.HORIZONTAL){if(side==direction)continue;var point=Vec3.atCenterOf(m.getBlockPos()).add(side.getStepX()*.2,0,side.getStepZ()*.2);TGPackets.NETWORK.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(point.x,point.y,point.z,10,m.getLevel().dimension())),new TGProjectilePackets.Effect("ChargeFlare2",point,0,0));}}
    private MachineWorkEffects(){}
}
