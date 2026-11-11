package techguns.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * One piece of cable holds up to CABLE_TRANSFER FE. Every tick it evens out its charge with neighbouring cables and
 * gives the rest to the blocks around it that take energy. No losses.
 */
public class EnergyCableEntity extends EnergySource {
    private final int transfer;
    public EnergyCableEntity(BlockPos pos,BlockState state){this(PowerBlocks.ENERGY_CABLE_ENTITY.get(),pos,state,state.getBlock() instanceof EnergyCableBlock b?b.transfer:PowerBlocks.CABLE_TRANSFER);}
    public EnergyCableEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,BlockPos pos,BlockState state,int transfer){super(type,pos,state,transfer,transfer);this.transfer=transfer;}
    @Override protected boolean takes(Direction side){return true;}
    public int transfer(){return transfer;}
    private int inflow,inflowNow;
    @Override protected void received(int amount){inflowNow+=amount;}
    /** Offered more than twice its rate at once, a cable burns out. */
    public static final int OVERLOAD=2;
    /** Burns the cable at be if offered is too much for it; true if it burned. */
    public static boolean overloads(net.minecraft.world.level.block.entity.BlockEntity be,int offered){
        if(!(be instanceof EnergyCableEntity cable)||offered<=(long)cable.transfer*OVERLOAD||cable.isRemoved())return false;
        cable.burn();return true;
    }
    public void burn(){
        if(!(level instanceof net.minecraft.server.level.ServerLevel server))return;
        var pos=worldPosition;var c=net.minecraft.world.phys.Vec3.atCenterOf(pos);
        server.removeBlock(pos,false);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,c.x,c.y,c.z,12,0.25,0.25,0.25,0.02);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,c.x,c.y,c.z,6,0.2,0.2,0.2,0);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,c.x,c.y,c.z,20,0.3,0.3,0.3,0.4);
        server.playSound(null,pos,net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE,net.minecraft.sounds.SoundSource.BLOCKS,1F,0.7F);
        server.playSound(null,pos,net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,net.minecraft.sounds.SoundSource.BLOCKS,0.6F,1.6F);
    }
    @Override protected int inputLimit(){return transfer;}
    public void tick(){
        inflow=inflowNow;inflowNow=0;
        if(level==null||energy.getEnergyStored()<=0)return;
        for(var side:Direction.values()){
            int stored=energy.getEnergyStored();if(stored<=0)break;
            var other=level.getBlockEntity(worldPosition.relative(side));if(other==null)continue;
            if(other instanceof EnergyCableEntity cable){
                if(cable.transfer<transfer&&overloads(cable,inflow))continue;
                int diff=(stored-cable.energy.getEnergyStored())/2;
                if(diff>0){int moved=cable.energy.receiveEnergy(diff,false);if(moved>0){cable.inflowNow+=moved;energy.extractEnergy(moved,false);cable.setChanged();setChanged();}}
                continue;
            }
            if(other instanceof EnergySource source&&!source.takes(side.getOpposite()))continue;
            var target=other.getCapability(ForgeCapabilities.ENERGY,side.getOpposite()).resolve();
            if(target.isEmpty()||!target.get().canReceive())continue;
            if(overloads(other,stored))continue;
            int given=target.get().receiveEnergy(stored,false);if(given>0){energy.extractEnergy(given,false);setChanged();}
        }
    }
}
