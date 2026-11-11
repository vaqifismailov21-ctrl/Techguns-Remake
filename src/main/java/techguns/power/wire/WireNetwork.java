package techguns.power.wire;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * All wires of one dimension (saved with it) and the way energy travels over them: from the point it enters at, over
 * the wires to every point that hands energy on (connectors to the block they sit on, transformers to their store),
 * nearest first. Each wire carries at most its kind's rate per tick and loses a little energy per block of length.
 */
public final class WireNetwork extends SavedData {
    private static final String NAME="techguns_wires";
    private final List<WireConnection> wires=new ArrayList<>();
    private final Map<WireNode,List<WireConnection>> byNode=new HashMap<>();
    private final Map<WireConnection,Integer> used=new IdentityHashMap<>();
    private long usedTick=-1;
    /** Game time each wire last carried energy (not saved). */
    private final Map<WireConnection,Long> live=new IdentityHashMap<>();

    public static WireNetwork get(ServerLevel level){return level.getDataStorage().computeIfAbsent(WireNetwork::load,WireNetwork::new,NAME);}
    public List<WireConnection> all(){return wires;}
    public List<WireConnection> at(WireNode node){return byNode.getOrDefault(node,List.of());}
    public List<WireConnection> at(BlockPos pos){var list=new ArrayList<WireConnection>();for(var w:wires)if(w.touches(pos))list.add(w);return list;}
    public boolean exists(WireConnection c){for(var w:at(c.a()))if(w.sameEnds(c))return true;return false;}

    public void add(ServerLevel level,WireConnection c){
        wires.add(c);index(c);setDirty();WirePackets.added(level,c);
    }
    /** Removes every wire on the block; returns them (to drop their coils). */
    public List<WireConnection> removeAt(ServerLevel level,BlockPos pos){
        var gone=at(pos);if(gone.isEmpty())return gone;
        wires.removeAll(gone);rebuild();setDirty();for(var c:gone)WirePackets.removed(level,c);
        return gone;
    }
    private void index(WireConnection c){byNode.computeIfAbsent(c.a(),k->new ArrayList<>()).add(c);byNode.computeIfAbsent(c.b(),k->new ArrayList<>()).add(c);}
    private void rebuild(){byNode.clear();used.clear();live.keySet().retainAll(new java.util.HashSet<>(wires));for(var w:wires)index(w);}
    /** Wires that carried energy during the last second. */
    public List<WireConnection> live(long now){var list=new ArrayList<WireConnection>();for(var e:live.entrySet())if(now-e.getValue()<=20)list.add(e.getKey());return list;}

    private void resetUsage(long tick){if(tick!=usedTick){used.clear();usedTick=tick;}}

    private record Target(WireNode node,List<WireConnection> path,float efficiency,int depth,float fill){}

    /**
     * Sends up to amount FE into the wires at a point. sinksOnly picks what may take it: storing sinks (transformers)
     * only when they are emptier than fromFill (-1: no storage at the source, so anything). Returns the energy used.
     */
    public int route(Level level,WireNode from,int amount,boolean simulate,float fromFill){
        if(amount<=0||!byNode.containsKey(from))return 0;
        long tick=level.getGameTime();
        // breadth first over the wires: the path to every point, with the share of energy that survives it
        var targets=new ArrayList<Target>();var seen=new HashMap<WireNode,Boolean>();seen.put(from,true);
        var queue=new ArrayDeque<Target>();queue.add(new Target(from,List.of(),1,0,0));
        while(!queue.isEmpty()){
            var t=queue.poll();
            for(var c:at(t.node())){
                var next=c.other(t.node());if(seen.containsKey(next))continue;seen.put(next,true);
                var path=new ArrayList<>(t.path());path.add(c);
                var step=new Target(next,path,t.efficiency()*c.tier().efficiency(c.length()),t.depth()+1,0);
                queue.add(step);targets.add(step);
            }
        }
        // ordinary takers first (nearest first), then stores that are emptier than the source, emptiest first
        var takers=new ArrayList<Object[]>();
        for(var t:targets){
            var state=level.getBlockState(t.node().pos());if(!(state.getBlock() instanceof WireHost host))continue;
            if(!level.isLoaded(t.node().pos()))continue;
            var sink=host.sink(level,t.node().pos(),state,t.node().point());if(sink==null||!sink.canReceive())continue;
            float fill=host.fill(level,t.node().pos());
            if(fill>=0&&fromFill>=0&&fill>=fromFill-0.001F)continue;
            if(t.node().pos().equals(from.pos()))continue;
            takers.add(new Object[]{t,sink,fill});
        }
        takers.sort((x,y)->{
            float fx=(float)x[2],fy=(float)y[2];
            if((fx<0)!=(fy<0))return fx<0?-1:1;
            if(fx>=0)return Float.compare(fx,fy);
            return Integer.compare(((Target)x[0]).depth(),((Target)y[0]).depth());
        });
        resetUsage(tick);
        int left=amount;var burnt=new ArrayList<WireConnection>();
        for(var o:takers){
            if(left<=0)break;
            var t=(Target)o[0];var sink=(IEnergyStorage)o[1];
            boolean broken=false;for(var c:t.path())if(burnt.contains(c)){broken=true;break;}
            if(broken)continue;
            int room=left;
            int arrives=(int)(room*t.efficiency());if(arrives<=0)continue;
            int taken=sink.receiveEnergy(arrives,simulate);if(taken<=0)continue;
            int spent=Math.min(room,(int)Math.ceil(taken/t.efficiency()));
            if(!simulate)for(var c:t.path()){live.put(c,tick);if(used.merge(c,spent,Integer::sum)>c.tier().rate&&!burnt.contains(c))burnt.add(c);}
            left-=spent;
        }
        if(!burnt.isEmpty()&&level instanceof ServerLevel server)for(var c:burnt)burn(server,c);
        return amount-left;
    }

    /** Too much energy through a wire in one tick: it melts with sparks and smoke along its length, and is gone. */
    public void burn(ServerLevel level,WireConnection c){
        if(!wires.remove(c))return;rebuild();setDirty();WirePackets.removed(level,c);
        var a=point(level,c.a());var b=point(level,c.b());if(a==null||b==null)return;
        double length=a.distanceTo(b),sag=WireTier.sag(c.tier(),length);int n=Math.max(4,(int)(length*1.5));
        for(int i=0;i<=n;i++){double t=i/(double)n;var p=a.lerp(b,t).add(0,-sag*4*t*(1-t),0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,4,0.08,0.08,0.08,0.3);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,p.x,p.y,p.z,2,0.05,0.05,0.05,0.01);
            if(i%3==0)level.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,p.x,p.y,p.z,1,0,0,0,0);}
        var mid=a.lerp(b,0.5);var pos=net.minecraft.core.BlockPos.containing(mid);
        level.playSound(null,pos,net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE,net.minecraft.sounds.SoundSource.BLOCKS,1.2F,0.6F);
        level.playSound(null,pos,net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,net.minecraft.sounds.SoundSource.BLOCKS,0.5F,1.8F);
    }
    static net.minecraft.world.phys.Vec3 point(Level level,WireNode node){
        var state=level.getBlockState(node.pos());
        return state.getBlock() instanceof WireHost host&&node.point()<host.points(state)?host.point(state,node.pos(),node.point()):null;
    }

    private static WireNetwork load(CompoundTag tag){
        var n=new WireNetwork();
        for(var t:tag.getList("Wires",Tag.TAG_COMPOUND))n.wires.add(WireConnection.load((CompoundTag)t));
        n.rebuild();return n;
    }
    @Override public CompoundTag save(CompoundTag tag){
        var list=new ListTag();for(var w:wires)list.add(w.save());tag.put("Wires",list);return tag;
    }
}
