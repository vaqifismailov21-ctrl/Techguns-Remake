package techguns.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import techguns.TGWorld;

/** Per-chunk order, random vein size, attempt count and exclusive height bounds from OreGenerator. */
public final class LegacyOreFeature extends Feature<NoneFeatureConfiguration> {
    public LegacyOreFeature(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){
        var world=context.level();
        if(!world.getLevel().dimension().equals(Level.OVERWORLD))return false;
        var origin=new BlockPos(context.origin().getX()&~15,0,context.origin().getZ()&~15);
        return generate(world,context.random(),origin)>0;
    }
    public static int generate(WorldGenLevel world,RandomSource random,BlockPos origin){
        int changed=0;
        for(var kind:new OreKind[]{OreKind.COPPER,OreKind.TIN,OreKind.LEAD,OreKind.URANIUM,OreKind.TITANIUM})if(kind.enabled()){
            int size=kind.veinBase+random.nextInt(kind.veinRandom);
            for(int i=0;i<kind.attempts;i++){
                int x=origin.getX()+random.nextInt(16),y=kind.minY+random.nextInt(kind.maxY-kind.minY),z=origin.getZ()+random.nextInt(16);
                changed+=vein(world,random,new BlockPos(x,y,z),TGWorld.ORES.get(kind).get().defaultBlockState(),size);
            }
        }
        return changed;
    }
    /** Swept ellipsoids used by the old minable generator. No modern deep-slate or copper balance changes. */
    public static int vein(WorldGenLevel world,RandomSource random,BlockPos origin,BlockState ore,int size){
        float angle=random.nextFloat()*(float)Math.PI;
        double x0=origin.getX()+8+Mth.sin(angle)*size/8f,x1=origin.getX()+8-Mth.sin(angle)*size/8f;
        double z0=origin.getZ()+8+Mth.cos(angle)*size/8f,z1=origin.getZ()+8-Mth.cos(angle)*size/8f;
        double y0=origin.getY()+random.nextInt(3)-2,y1=origin.getY()+random.nextInt(3)-2;
        int changed=0;var cursor=new BlockPos.MutableBlockPos();
        for(int step=0;step<size;step++){
            float t=(float)step/size;
            double x=x0+(x1-x0)*t,y=y0+(y1-y0)*t,z=z0+(z1-z0)*t;
            double n=random.nextDouble()*size/16d,diameter=(Mth.sin((float)Math.PI*t)+1)*n+1;
            for(int bx=Mth.floor(x-diameter/2);bx<=Mth.floor(x+diameter/2);bx++){
                double nx=(bx+.5-x)/(diameter/2);if(nx*nx>=1)continue;
                for(int by=Mth.floor(y-diameter/2);by<=Mth.floor(y+diameter/2);by++){
                    double ny=(by+.5-y)/(diameter/2);if(nx*nx+ny*ny>=1)continue;
                    for(int bz=Mth.floor(z-diameter/2);bz<=Mth.floor(z+diameter/2);bz++){
                        double nz=(bz+.5-z)/(diameter/2);if(nx*nx+ny*ny+nz*nz>=1)continue;
                        cursor.set(bx,by,bz);
                        // Original predicate targets stone and its natural variants, not all ore-replaceable rocks.
                        var state=world.getBlockState(cursor);
                        if(state.is(Blocks.STONE)||state.is(Blocks.GRANITE)||state.is(Blocks.DIORITE)||state.is(Blocks.ANDESITE)){
                            if(world.setBlock(cursor,ore,2))changed++;
                        }
                    }
                }
            }
        }
        return changed;
    }
    /** Same random use and swept-ellipsoid math as {@link #vein} around the origin, for yield estimates in solid stone. */
    public static java.util.Set<BlockPos> veinShape(RandomSource random,int size){
        float angle=random.nextFloat()*(float)Math.PI;
        double x0=8+Mth.sin(angle)*size/8f,x1=8-Mth.sin(angle)*size/8f,z0=8+Mth.cos(angle)*size/8f,z1=8-Mth.cos(angle)*size/8f,y0=random.nextInt(3)-2,y1=random.nextInt(3)-2;
        var shape=new java.util.HashSet<BlockPos>();
        for(int step=0;step<size;step++){
            float t=(float)step/size;double x=x0+(x1-x0)*t,y=y0+(y1-y0)*t,z=z0+(z1-z0)*t,n=random.nextDouble()*size/16d,diameter=(Mth.sin((float)Math.PI*t)+1)*n+1;
            for(int bx=Mth.floor(x-diameter/2);bx<=Mth.floor(x+diameter/2);bx++){double nx=(bx+.5-x)/(diameter/2);if(nx*nx>=1)continue;
                for(int by=Mth.floor(y-diameter/2);by<=Mth.floor(y+diameter/2);by++){double ny=(by+.5-y)/(diameter/2);if(nx*nx+ny*ny>=1)continue;
                    for(int bz=Mth.floor(z-diameter/2);bz<=Mth.floor(z+diameter/2);bz++){double nz=(bz+.5-z)/(diameter/2);if(nx*nx+ny*ny+nz*nz<1)shape.add(new BlockPos(bx,by,bz));}}}
        }
        return shape;
    }
}
