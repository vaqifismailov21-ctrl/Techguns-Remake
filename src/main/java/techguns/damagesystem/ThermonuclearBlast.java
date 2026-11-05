package techguns.damagesystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import techguns.Techguns;

/**
 * Thermonuclear rocket. Everything inside KILL dies regardless of cover, damage falls off to the edge of BLAST.
 * The crater is dug out shell by shell with a block budget per tick, so even the large one does not stall the server.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class ThermonuclearBlast {
    public static final int CRATER=96, KILL=150, BLAST=300, BLOCKS_PER_TICK=12000;
    private static final List<ThermonuclearBlast> ACTIVE=new ArrayList<>();
    private final ServerLevel level;private final BlockPos centre;private final long seed;
    private int radius;private long[] shell=new long[0];private int index;

    private ThermonuclearBlast(ServerLevel level,BlockPos centre){this.level=level;this.centre=centre;this.seed=centre.asLong();}

    public static void detonate(Level world,LivingEntity shooter,Entity projectile,TGDamageSource source,Vec3 at,double damage,boolean terrain){
        if(!(world instanceof ServerLevel level))return;
        var tgs=TGDamageSource.copyWithNewEnt(source,projectile,shooter);
        for(var entity:level.getEntities(projectile,new AABB(at,at).inflate(BLAST))){
            if(entity.ignoreExplosion()||entity.isSpectator()||!entity.isAlive()||!entity.isPickable())continue;
            double distance=at.distanceTo(entity.position());if(distance>BLAST)continue;
            double amount=distance<=KILL?damage:damage*Math.pow(1-(distance-KILL)/(BLAST-KILL),3);
            entity.hurt(tgs,(float)Math.min(Float.MAX_VALUE/2,Math.max(1,amount)));
            if(distance<BLAST*0.7)entity.setSecondsOnFire(20);
            var push=entity.position().subtract(at).normalize().scale(4*(1-distance/BLAST));entity.push(push.x,0.8+push.y,push.z);
        }
        if(techguns.TGRadiationSystem.isEnabled())level.addFreshEntity(new techguns.entities.special.EntityRadiation(level,at.x,at.y,at.z,24000,KILL,10,BLAST,4));
        if(terrain)ACTIVE.add(new ThermonuclearBlast(level,BlockPos.containing(at)));
    }

    /** Ragged edge: each direction of the crater ends between 80 % and 100 % of CRATER. */
    private double edge(int x,int y,int z){
        int cx=Math.floorDiv(x,6),cy=Math.floorDiv(y,6),cz=Math.floorDiv(z,6);
        long h=seed^(cx*73856093L)^(cy*19349663L)^(cz*83492791L);h^=h>>>13;h*=0x5bd1e995L;h^=h>>>15;
        return CRATER*(0.8+0.2*((h&1023)/1023.0));
    }
    /** All positions with radius-1 < distance <= radius. */
    private long[] shell(int r){
        var list=new it.unimi.dsi.fastutil.longs.LongArrayList();
        int rr=r*r,inner=(r-1)*(r-1);
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
            int q=x*x+z*z;if(q>rr)continue;
            int top=(int)Math.floor(Math.sqrt(rr-q));
            int low=inner-q<0?0:(int)Math.floor(Math.sqrt(inner-q))+1;
            for(int y=low;y<=top;y++){
                if(r>CRATER*0.8&&Math.sqrt(q+y*y)>edge(x,y,z))continue;
                list.add(BlockPos.asLong(centre.getX()+x,centre.getY()+y,centre.getZ()+z));
                if(y!=0){if(r>CRATER*0.8&&Math.sqrt(q+y*y)>edge(x,-y,z))continue;list.add(BlockPos.asLong(centre.getX()+x,centre.getY()-y,centre.getZ()+z));}
            }
        }
        return list.toLongArray();
    }
    private boolean step(){
        var pos=new BlockPos.MutableBlockPos();int budget=BLOCKS_PER_TICK,checks=200_000;
        while(budget>0&&checks-->0){
            if(index>=shell.length){if(radius>=CRATER)return true;radius++;shell=shell(radius);index=0;continue;}
            pos.set(shell[index++]);
            if(!level.isInWorldBounds(pos)||!level.isLoaded(pos))continue;
            var state=level.getBlockState(pos);
            if(state.isAir()||state.getDestroySpeed(level,pos)<0)continue;
            if(level.getBlockEntity(pos)!=null)level.removeBlockEntity(pos);
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),2|16);budget--;
        }
        return false;
    }
    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event){
        if(event.phase!=TickEvent.Phase.END||ACTIVE.isEmpty())return;
        for(Iterator<ThermonuclearBlast> it=ACTIVE.iterator();it.hasNext();){var blast=it.next();if(blast.level==event.level&&blast.step())it.remove();}
    }
}
