package techguns.damagesystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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
 * Thermonuclear blast (rocket and /tgnuke). Everything within 1.5x the crater radius dies regardless of cover,
 * damage falls off to 3x the crater radius. The crater is dug shell by shell with a block budget per tick.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID)
public final class ThermonuclearBlast {
    public static final int CRATER=96, MAX_CRATER=1000, BLOCKS_PER_TICK=16000;
    private static final List<ThermonuclearBlast> ACTIVE=new ArrayList<>();
    private final ServerLevel level;private final BlockPos centre;private final long seed;private final int crater;
    /** Cursor of the shell being dug: radius and column; nothing is stored per block, so even a 1000 block crater needs no memory. */
    private int radius,cx,cz;private boolean started;

    private ThermonuclearBlast(ServerLevel level,BlockPos centre,int crater){this.level=level;this.centre=centre;this.seed=centre.asLong();this.crater=crater;}
    public static int killRadius(int crater){return crater*3/2;}
    public static int blastRadius(int crater){return crater*3;}

    /** Rocket hit: the rocket's damage source and its 96 block crater. */
    public static void detonate(Level world,LivingEntity shooter,Entity projectile,TGDamageSource source,Vec3 at,double damage,boolean terrain){
        if(world instanceof ServerLevel level)detonate(level,at,TGDamageSource.copyWithNewEnt(source,projectile,shooter),projectile,CRATER,damage,terrain);
    }
    /** Any size, from 1 to MAX_CRATER. */
    public static void detonate(ServerLevel level,Vec3 at,DamageSource source,Entity except,int crater,double damage,boolean terrain){
        crater=Math.max(1,Math.min(MAX_CRATER,crater));int kill=killRadius(crater),blast=blastRadius(crater);
        for(var entity:level.getEntities(except,new AABB(at,at).inflate(blast))){
            if(entity.ignoreExplosion()||entity.isSpectator()||!entity.isAlive()||!entity.isPickable())continue;
            double distance=at.distanceTo(entity.position());if(distance>blast)continue;
            double amount=distance<=kill?damage:damage*Math.pow(1-(distance-kill)/(double)(blast-kill),3);
            entity.hurt(source,(float)Math.min(1e30,Math.max(1,amount)));
            if(distance<blast*0.7)entity.setSecondsOnFire(20);
            var push=entity.position().subtract(at).normalize().scale(4*(1-distance/blast));entity.push(push.x,0.8+push.y,push.z);
        }
        if(techguns.TGRadiationSystem.isEnabled())level.addFreshEntity(new techguns.entities.special.EntityRadiation(level,at.x,at.y,at.z,24000,kill,10,blast,4));
        double range=Math.max(512,blast*2);float scale=Math.max(1,crater/24F);
        techguns.TGProjectilePackets.farEffect(level,"NukeExplosion",at,scale,range);
        for(int i=0;i<8;i++){double a=i*Math.PI/4,r=crater*0.45;techguns.TGProjectilePackets.farEffect(level,"NukeExplosion",at.add(Math.cos(a)*r,crater*0.1,Math.sin(a)*r),scale*0.6F,range);}
        level.playSound(null,at.x,at.y,at.z,techguns.TGSounds.NUKE_EXPLOSION.get(),net.minecraft.sounds.SoundSource.BLOCKS,(float)(range/16),0.5F);
        if(terrain)ACTIVE.add(new ThermonuclearBlast(level,BlockPos.containing(at),crater));
    }

    /** Ragged edge: each direction ends between 80 % and 100 % of the crater radius. */
    private double edge(int x,int y,int z){
        int cell=Math.max(4,crater/16);int cx=Math.floorDiv(x,cell),cy=Math.floorDiv(y,cell),cz=Math.floorDiv(z,cell);
        long h=seed^(cx*73856093L)^(cy*19349663L)^(cz*83492791L);h^=h>>>13;h*=0x5bd1e995L;h^=h>>>15;
        return crater*(0.8+0.2*((h&1023)/1023.0));
    }
    private final BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
    private int budget,checks;
    /** Removes one block if it is breakable; counts against the tick budget. */
    private void dig(int x,int y,int z){
        checks--;
        pos.set(centre.getX()+x,centre.getY()+y,centre.getZ()+z);
        if(!level.isInWorldBounds(pos)||!level.isLoaded(pos))return;
        var state=level.getBlockState(pos);
        if(state.isAir()||state.getDestroySpeed(level,pos)<0)return;
        if(level.getBlockEntity(pos)!=null)level.removeBlockEntity(pos);
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),2|16);budget--;
    }
    /** One column of the shell radius-1 < distance <= radius, inside the ragged edge. */
    private void column(int r,int x,int z){
        long rr=(long)r*r,inner=(long)(r-1)*(r-1),q=(long)x*x+(long)z*z;if(q>rr)return;
        // columns entirely below the world or above the build limit hold nothing to dig
        if(centre.getY()+r<level.getMinBuildHeight()||centre.getY()-r>level.getMaxBuildHeight())return;
        boolean rim=r>crater*0.8;int top=(int)Math.floor(Math.sqrt(rr-q));int low=inner-q<0?0:(int)Math.floor(Math.sqrt(inner-q))+1;
        for(int y=low;y<=top;y++){
            double d=Math.sqrt(q+(long)y*y);
            if(!rim||d<=edge(x,y,z))dig(x,y,z);
            if(y!=0&&(!rim||d<=edge(x,-y,z)))dig(x,-y,z);
        }
    }
    private boolean step(){
        budget=BLOCKS_PER_TICK;checks=300_000;
        if(!started){started=true;dig(0,0,0);radius=1;cx=-1;cz=-1;}
        while(budget>0&&checks>0){
            checks--;column(radius,cx,cz);
            if(++cz>radius){if(++cx>radius){if(radius>=crater)return true;radius++;cx=-radius;}cz=-radius;}
        }
        return false;
    }
    /** Stops every crater still being dug (used by /tgnuke stop). */
    public static int stopAll(){int n=ACTIVE.size();ACTIVE.clear();return n;}
    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event){
        if(event.phase!=TickEvent.Phase.END||ACTIVE.isEmpty())return;
        for(Iterator<ThermonuclearBlast> it=ACTIVE.iterator();it.hasNext();){var blast=it.next();if(blast.level==event.level&&blast.step())it.remove();}
    }
}
