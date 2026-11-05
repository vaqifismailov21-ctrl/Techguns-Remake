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
 * Thermonuclear rocket: a hundred times the tactical nuke's damage. Everything inside the blast radius takes it
 * regardless of cover, and the crater is dug out a few shells per tick so the server does not stall.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class ThermonuclearBlast {
    public static final int CRATER=32, KILL=48, BLAST=100, SHELLS_PER_TICK=1;
    private static final List<ThermonuclearBlast> ACTIVE=new ArrayList<>();
    private static int[][] offsets;
    private final ServerLevel level;private final BlockPos centre;private int index;private final long seed;

    private ThermonuclearBlast(ServerLevel level,BlockPos centre){this.level=level;this.centre=centre;this.seed=centre.asLong();}

    public static void detonate(Level world,LivingEntity shooter,Entity projectile,TGDamageSource source,Vec3 at,double damage,boolean terrain){
        if(!(world instanceof ServerLevel level))return;
        var tgs=TGDamageSource.copyWithNewEnt(source,projectile,shooter);
        for(var entity:level.getEntities(projectile,new AABB(at,at).inflate(BLAST))){
            if(entity.ignoreExplosion()||entity.isSpectator()||!entity.isAlive()||!entity.isPickable())continue;
            double distance=at.distanceTo(entity.position());if(distance>BLAST)continue;
            double amount=distance<=KILL?damage:damage*Math.pow(1-(distance-KILL)/(BLAST-KILL),2);
            entity.hurt(tgs,(float)Math.max(1,amount));
            if(distance<BLAST*0.75)entity.setSecondsOnFire(15);
            var push=entity.position().subtract(at).normalize().scale(3*(1-distance/BLAST));entity.push(push.x,0.6+push.y,push.z);
        }
        if(techguns.TGRadiationSystem.isEnabled())level.addFreshEntity(new techguns.entities.special.EntityRadiation(level,at.x,at.y,at.z,12000,KILL,10,BLAST,4));
        if(terrain)ACTIVE.add(new ThermonuclearBlast(level,BlockPos.containing(at)));
    }

    /** Offsets of a sphere of radius CRATER, nearest first. */
    private static int[][] offsets(){
        if(offsets!=null)return offsets;
        var list=new ArrayList<int[]>();
        for(int x=-CRATER;x<=CRATER;x++)for(int y=-CRATER;y<=CRATER;y++)for(int z=-CRATER;z<=CRATER;z++){int d=x*x+y*y+z*z;if(d<=CRATER*CRATER)list.add(new int[]{x,y,z,d});}
        list.sort((a,b)->Integer.compare(a[3],b[3]));
        return offsets=list.toArray(new int[0][]);
    }
    /** Ragged edge: each direction ends somewhere between 80 % and 100 % of the radius. */
    private boolean inside(int[] o){
        double d=Math.sqrt(o[3]);if(d<CRATER*0.8)return true;
        long h=seed^(o[0]*73856093L)^(o[1]*19349663L)^(o[2]*83492791L);h^=h>>>13;h*=0x5bd1e995L;h^=h>>>15;
        double edge=CRATER*(0.8+0.2*((h&1023)/1023.0));return d<=edge;
    }
    private boolean step(){
        var all=offsets();if(index>=all.length)return true;
        int limit=Math.min(all.length,index+1);double radius=Math.sqrt(all[index][3])+SHELLS_PER_TICK;
        while(limit<all.length&&Math.sqrt(all[limit][3])<radius)limit++;
        var pos=new BlockPos.MutableBlockPos();
        for(;index<limit;index++){
            var o=all[index];if(!inside(o))continue;
            pos.set(centre.getX()+o[0],centre.getY()+o[1],centre.getZ()+o[2]);
            if(!level.isInWorldBounds(pos)||!level.isLoaded(pos))continue;
            var state=level.getBlockState(pos);
            if(state.isAir()||state.getDestroySpeed(level,pos)<0)continue;
            if(level.getBlockEntity(pos)!=null)level.removeBlockEntity(pos);
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),2|16);
        }
        return index>=all.length;
    }
    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event){
        if(event.phase!=TickEvent.Phase.END||ACTIVE.isEmpty())return;
        for(Iterator<ThermonuclearBlast> it=ACTIVE.iterator();it.hasNext();){var blast=it.next();if(blast.level==event.level&&blast.step())it.remove();}
    }
}
