package techguns.client.ctm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import techguns.Techguns;

/**
 * Built-in connected textures. A texture "x" connects when "x-ctm" (2x2 sheet: full, vertical, horizontal, inner corners) exists.
 * Every quad is split into four corners; each corner looks at its two edges and the diagonal on the quad's own plane,
 * so step edges of stairs and slab tops keep their borders while coplanar faces of any shape join.
 */
@Mod.EventBusSubscriber(modid=Techguns.MODID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ConnectedTextures {
    private ConnectedTextures(){}
    private static final String[] SKIP={"drill_rod"};
    private static volatile Set<ResourceLocation> connected=Set.of();
    private static volatile Map<BlockState,Set<ResourceLocation>> groups=Map.of();
    private static final Map<BlockState,List<AABB>> shapes=new ConcurrentHashMap<>();
    private static final Map<ResourceLocation,TextureAtlasSprite> sheets=new ConcurrentHashMap<>();
    private static final double EDGE=0.1,DEPTH=0.01;

    /** Off: connected textures come from the CTM / Fusion / Athena compatibility (ConnectedTexturesCompat), as the user asked. */
    public static final boolean ENABLED=false;
    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event){
        if(!ENABLED)return;
        var resources=Minecraft.getInstance().getResourceManager();
        var known=new HashMap<ResourceLocation,Boolean>();
        var newGroups=new HashMap<BlockState,Set<ResourceLocation>>();
        var models=event.getModels();var random=RandomSource.create(42);
        for(var block:ForgeRegistries.BLOCKS){
            var id=ForgeRegistries.BLOCKS.getKey(block);if(id==null||!id.getNamespace().equals(Techguns.MODID))continue;
            for(var state:block.getStateDefinition().getPossibleStates()){
                var location=BlockModelShaper.stateToModelLocation(state);var model=models.get(location);if(model==null)continue;
                var names=new HashSet<ResourceLocation>();
                try{
                    for(var side:sides())for(var quad:model.getQuads(state,side,random,ModelData.EMPTY,null)){
                        var name=quad.getSprite().contents().name();
                        if(known.computeIfAbsent(name,n->hasSheet(resources,n)))names.add(name);
                    }
                }catch(RuntimeException e){continue;}
                if(names.isEmpty())continue;
                newGroups.put(state,names);
                if(!(model instanceof ConnectedModel))models.put(location,new ConnectedModel(model));
            }
        }
        var set=new HashSet<ResourceLocation>();known.forEach((k,v)->{if(v)set.add(k);});
        connected=set;groups=newGroups;shapes.clear();sheets.clear();
    }
    private static Direction[] sides(){var all=new Direction[7];System.arraycopy(Direction.values(),0,all,0,6);return all;}
    private static boolean hasSheet(net.minecraft.server.packs.resources.ResourceManager resources,ResourceLocation name){
        if(!name.getNamespace().equals(Techguns.MODID))return false;
        for(var skip:SKIP)if(name.getPath().endsWith(skip))return false;
        return resources.getResource(ResourceLocation.fromNamespaceAndPath(name.getNamespace(),"textures/"+name.getPath()+"-ctm.png")).isPresent();
    }
    static boolean isConnected(TextureAtlasSprite sprite){return connected.contains(sprite.contents().name());}
    /** One tile of the sheet; the atlas unstitches the four tiles into own sprites so mipmaps do not bleed the neighbouring tile into the block joints. */
    private static TextureAtlasSprite tile(ResourceLocation base,int col,int row){
        var id=ResourceLocation.fromNamespaceAndPath(base.getNamespace(),base.getPath()+"-ctm_"+col+row);
        return sheets.computeIfAbsent(id,b->Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(b));
    }

    /** Splits one quad into its four corners with the matching sheet tile. */
    static void split(BakedQuad quad,BlockAndTintGetter level,BlockPos pos,List<BakedQuad> out){
        var sprite=quad.getSprite();int[] data=quad.getVertices();int stride=data.length/4;
        float[] x=new float[4],y=new float[4],z=new float[4],u=new float[4],v=new float[4];
        float du=sprite.getU1()-sprite.getU0(),dv=sprite.getV1()-sprite.getV0();
        for(int i=0;i<4;i++){int o=i*stride;x[i]=Float.intBitsToFloat(data[o]);y[i]=Float.intBitsToFloat(data[o+1]);z[i]=Float.intBitsToFloat(data[o+2]);
            u[i]=(Float.intBitsToFloat(data[o+4])-sprite.getU0())/du*16;v[i]=(Float.intBitsToFloat(data[o+5])-sprite.getV0())/dv*16;}
        float u0=Math.min(Math.min(u[0],u[1]),Math.min(u[2],u[3])),u1=Math.max(Math.max(u[0],u[1]),Math.max(u[2],u[3]));
        float v0=Math.min(Math.min(v[0],v[1]),Math.min(v[2],v[3])),v1=Math.max(Math.max(v[0],v[1]),Math.max(v[2],v[3]));
        if(u1-u0<0.01F||v1-v0<0.01F){out.add(quad);return;}
        float um=(u0+u1)/2,vm=(v0+v1)/2;
        // corner vertex by texture side: c[cu][cv]
        int[][] corner=new int[2][2];int[] cu=new int[4],cv=new int[4];
        for(int i=0;i<4;i++){cu[i]=u[i]<um?0:1;cv[i]=v[i]<vm?0:1;corner[cu[i]][cv[i]]=i;}
        var plane=new Plane(x,y,z,corner,u0,u1,v0,v1);
        var normal=quad.getDirection().getNormal();var name=sprite.contents().name();
        for(int pu=0;pu<2;pu++)for(int pv=0;pv<2;pv++){
            double su=pu==0?u0-EDGE:u1+EDGE,sv=pv==0?(v0+vm)/2:(vm+v1)/2;
            double tu=pu==0?(u0+um)/2:(um+u1)/2,tv=pv==0?v0-EDGE:v1+EDGE;
            boolean h=joins(level,pos,plane.at(su,sv),normal,name),w=joins(level,pos,plane.at(tu,tv),normal,name);
            boolean d=h&&w&&joins(level,pos,plane.at(su,tv),normal,name);
            TextureAtlasSprite target;
            if(!h&&!w)target=sprite;else if(h&&w)target=d?tile(name,0,0):tile(name,1,1);else if(w)target=tile(name,1,0);else target=tile(name,0,1);
            float lo=pu==0?u0:um,hi=pu==0?um:u1,vlo=pv==0?v0:vm,vhi=pv==0?vm:v1;
            int[] piece=data.clone();
            for(int i=0;i<4;i++){
                float pu2=cu[i]==0?lo:hi,pv2=cv[i]==0?vlo:vhi;double[] p=plane.at(pu2,pv2);int o=i*stride;
                piece[o]=Float.floatToRawIntBits((float)p[0]);piece[o+1]=Float.floatToRawIntBits((float)p[1]);piece[o+2]=Float.floatToRawIntBits((float)p[2]);
                float texU=pu==0?pu2-u0:16-(u1-pu2),texV=pv==0?pv2-v0:16-(v1-pv2);
                piece[o+4]=Float.floatToRawIntBits(target.getU(texU));piece[o+5]=Float.floatToRawIntBits(target.getV(texV));
            }
            out.add(new BakedQuad(piece,quad.getTintIndex(),quad.getDirection(),target,quad.isShade(),quad.hasAmbientOcclusion()));
        }
    }
    /** A point on the face plane joins when the same texture continues there on the same plane and is not covered by that texture's block. */
    private static boolean joins(BlockAndTintGetter level,BlockPos pos,double[] p,net.minecraft.core.Vec3i n,ResourceLocation name){
        double wx=pos.getX()+p[0],wy=pos.getY()+p[1],wz=pos.getZ()+p[2];
        if(!has(level,wx-n.getX()*DEPTH,wy-n.getY()*DEPTH,wz-n.getZ()*DEPTH,name))return false;
        return !has(level,wx+n.getX()*DEPTH,wy+n.getY()*DEPTH,wz+n.getZ()*DEPTH,name);
    }
    private static boolean has(BlockAndTintGetter level,double x,double y,double z,ResourceLocation name){
        var at=BlockPos.containing(x,y,z);var state=level.getBlockState(at);var group=groups.get(state);
        if(group==null||!group.contains(name))return false;
        double lx=x-at.getX(),ly=y-at.getY(),lz=z-at.getZ();
        for(var box:shapes.computeIfAbsent(state,s->s.getShape(EmptyBlockGetter.INSTANCE,BlockPos.ZERO).toAabbs()))
            if(lx>=box.minX&&lx<=box.maxX&&ly>=box.minY&&ly<=box.maxY&&lz>=box.minZ&&lz<=box.maxZ)return true;
        return false;
    }
    private record Plane(float[] x,float[] y,float[] z,int[][] c,float u0,float u1,float v0,float v1){
        double[] at(double u,double v){
            double t=(u-u0)/(u1-u0),s=(v-v0)/(v1-v0);double[] r=new double[3];
            float[][] axes={x,y,z};
            for(int k=0;k<3;k++){float[] a=axes[k];double top=a[c[0][0]]+(a[c[1][0]]-a[c[0][0]])*t,bottom=a[c[0][1]]+(a[c[1][1]]-a[c[0][1]])*t;r[k]=top+(bottom-top)*s;}
            return r;
        }
    }
}
